package de.joernott.c0denet.core.mail;

import de.joernott.c0denet.core.http.NetError;
import de.joernott.c0denet.core.http.NetException;
import jakarta.mail.Flags;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.BodyTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.RecipientStringTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The criteria of IMAP_SEARCH: the search keys of IMAP that programs need, combined by spaces (all must match), e.g.
 * {@code UNSEEN FROM "alice" SINCE 1-Nov-2026}.
 */
final class MailSearch {
    private MailSearch() {}

    /**
     * The search term of criteria, or null for ALL.
     *
     * @throws NetException INVALID_ARGUMENT for a key it doesn't know or a value that is missing
     */
    static SearchTerm parse(String criteria) {
        List<String> tokens = tokens(criteria);
        List<SearchTerm> terms = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String key = tokens.get(i).toUpperCase(Locale.ROOT);
            switch (key) {
                case "ALL" -> {}
                case "SEEN" -> terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), true));
                case "UNSEEN" -> terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
                case "FLAGGED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));
                case "UNFLAGGED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), false));
                case "ANSWERED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.ANSWERED), true));
                case "UNANSWERED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.ANSWERED), false));
                case "DELETED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.DELETED), true));
                case "UNDELETED" -> terms.add(new FlagTerm(new Flags(Flags.Flag.DELETED), false));
                case "DRAFT" -> terms.add(new FlagTerm(new Flags(Flags.Flag.DRAFT), true));
                case "FROM" -> terms.add(new FromStringTerm(value(tokens, ++i, key)));
                case "TO" ->
                    terms.add(new RecipientStringTerm(jakarta.mail.Message.RecipientType.TO, value(tokens, ++i, key)));
                case "CC" ->
                    terms.add(new RecipientStringTerm(jakarta.mail.Message.RecipientType.CC, value(tokens, ++i, key)));
                case "SUBJECT" -> terms.add(new SubjectTerm(value(tokens, ++i, key)));
                case "BODY" -> terms.add(new BodyTerm(value(tokens, ++i, key)));
                case "SINCE" -> terms.add(new ReceivedDateTerm(ComparisonTerm.GE, date(value(tokens, ++i, key))));
                case "BEFORE" -> terms.add(new ReceivedDateTerm(ComparisonTerm.LT, date(value(tokens, ++i, key))));
                case "ON" -> terms.add(new ReceivedDateTerm(ComparisonTerm.EQ, date(value(tokens, ++i, key))));
                default -> throw invalid("IMAP_SEARCH doesn't know the criterion " + tokens.get(i));
            }
        }
        if (terms.isEmpty()) {
            return null;
        }
        return terms.size() == 1 ? terms.get(0) : new AndTerm(terms.toArray(new SearchTerm[0]));
    }

    /** The words of the criteria; text in double quotes is one word, without its quotes. */
    static List<String> tokens(String criteria) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = null;
        boolean quoted = false;
        for (int i = 0; i < criteria.length(); i++) {
            char c = criteria.charAt(i);
            if (quoted) {
                if (c == '"') {
                    quoted = false;
                    tokens.add(current.toString());
                    current = null;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                if (current != null) {
                    tokens.add(current.toString());
                }
                current = new StringBuilder();
                quoted = true;
            } else if (Character.isWhitespace(c)) {
                if (current != null) {
                    tokens.add(current.toString());
                    current = null;
                }
            } else {
                if (current == null) {
                    current = new StringBuilder();
                }
                current.append(c);
            }
        }
        if (quoted) {
            throw invalid("The criteria of IMAP_SEARCH have a \" without its end");
        }
        if (current != null) {
            tokens.add(current.toString());
        }
        return tokens;
    }

    private static String value(List<String> tokens, int index, String key) {
        if (index >= tokens.size()) {
            throw invalid("The criterion " + key + " of IMAP_SEARCH needs a value");
        }
        return tokens.get(index);
    }

    /** A date like 1-Nov-2026, as IMAP writes it. */
    private static Date date(String text) {
        SimpleDateFormat format = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
        format.setLenient(false);
        try {
            return format.parse(text);
        } catch (ParseException e) {
            throw invalid("A date of IMAP_SEARCH looks like 1-Nov-2026, not " + text);
        }
    }

    private static NetException invalid(String message) {
        return new NetException(NetError.INVALID_ARGUMENT, message);
    }
}

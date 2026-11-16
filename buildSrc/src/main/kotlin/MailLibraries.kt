/**
 * The libraries of c0de_net/mail (docs/concept.md#the-library-c0de_netmail): Angus Mail, the implementation of
 * Jakarta Mail, with its dependencies. The mod nodes embed them with jar-in-jar (which is not transitive), the runner
 * jar contains their classes. Keep the versions in line with the dependencies of angus-mail in core/build.gradle.kts.
 */
val mailLibraries = listOf(
    "org.eclipse.angus:angus-mail:2.0.5",
    "jakarta.mail:jakarta.mail-api:2.1.5",
    "org.eclipse.angus:angus-activation:2.0.3",
    "jakarta.activation:jakarta.activation-api:2.1.4",
)

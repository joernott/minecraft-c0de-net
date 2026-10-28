package de.joernott.c0denet.gametest.neoforge;

//? if neoforge && >=1.21.11 {
/*import de.joernott.c0denet.C0deNet;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/^*
 * Registers the GameTests with NeoForge since 1.21.11, as in c0de: the GameTest framework of Minecraft keeps test
 * functions and test instances in registries since 1.21.5. The test methods of {@link NeoForgeGameTests} keep the
 * annotations of the older versions; replacements in stonecutter.gradle.kts point them to the annotations below.
 ^/
public final class GameTestRegistration {
    /^* A test method: {@code template} is the structure, {@code timeoutTicks} the maximum number of ticks. ^/
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface GameTest {
        String template();

        int timeoutTicks() default 100;
    }

    /^* Marks the class of the test methods, like the annotation of NeoForge before 1.21.5. ^/
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface GameTestHolder {
        String value();
    }

    /^* Like the annotation of NeoForge before 1.21.5: the templates are IDs of the mod. ^/
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface PrefixGameTestTemplate {
        boolean value();
    }

    private GameTestRegistration() {}

    /^* Registers the test functions now and the test instances when NeoForge asks for them. ^/
    public static void register(IEventBus modEventBus) {
        List<Method> tests = new ArrayList<>();
        DeferredRegister<Consumer<GameTestHelper>> functions =
                DeferredRegister.create(Registries.TEST_FUNCTION, C0deNet.MOD_ID);
        for (Method method : NeoForgeGameTests.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GameTest.class) && Modifier.isStatic(method.getModifiers())) {
                tests.add(method);
                Consumer<GameTestHelper> function = helper -> run(method, helper);
                functions.register(name(method), () -> function);
            }
        }
        functions.register(modEventBus);
        modEventBus.addListener(RegisterGameTestsEvent.class, event -> {
            // The type of the holder has a type parameter since 26.1
            var environment =
                    event.registerEnvironment(C0deNet.id("default"), new TestEnvironmentDefinition.AllOf(List.of()));
            for (Method method : tests) {
                GameTest test = method.getAnnotation(GameTest.class);
                event.registerTest(C0deNet.id(name(method)), new FunctionGameTestInstance(
                        ResourceKey.create(Registries.TEST_FUNCTION, C0deNet.id(name(method))),
                        new TestData<>(environment, C0deNet.id(test.template()), test.timeoutTicks(), 0, true)));
            }
        });
    }

    /^* The ID of a test: the name of its method in snake case. ^/
    private static String name(Method method) {
        return method.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }

    private static void run(Method method, GameTestHelper helper) {
        try {
            method.invoke(null, helper);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
*///?}

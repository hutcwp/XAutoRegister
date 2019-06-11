package me.hutcwp.api;

/**
 * ×¢ÈëÆ÷
 */
public class Injector {

    public static void autoRegister(BaseManager manager) {
        getAutoRegisterInject(manager).inject(manager);
    }

    private static Inject getAutoRegisterInject(BaseManager manager) {
        try {
            String suffix = "$AutoInject";
            String name = manager.getClass().getName() + suffix;
            Class injectorClazz = Class.forName(name);
            return (Inject) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "ClassNotFoundException :" +
                            " Manager$$AutoInject is not found ,make sure you have defined component with annotation " +
                            "@AutoRegister");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("There have a exception from Inject [AutoRegister]");
    }
}

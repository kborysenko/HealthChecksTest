package test.UIPages;

import com.codeborne.selenide.Selenide;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Map;

public abstract class BasePage {
    public static <PageObjectClass extends BasePage> PageObjectClass navigate(Class<PageObjectClass> pageObjectClass) {
        return navigate(pageObjectClass, Collections.emptyMap());
    }

    public static <PageObjectClass extends BasePage> PageObjectClass navigate(Class<PageObjectClass> pageObjectClass, Map<String, String>... parameters) {
        PageObjectClass page = initialize(pageObjectClass);
        Selenide.open(page.createUrl());
        return page;
    }

    public static <PageObjectClass extends BasePage> PageObjectClass open(Class<PageObjectClass> pageObjectClass) {
        return verify(navigate(pageObjectClass, Collections.emptyMap()));
    }

    public static <PageObjectClass extends BasePage> PageObjectClass open(Class<PageObjectClass> pageObjectClass, Map<String, String>... params) {
        return verify(navigate(pageObjectClass, params));
    }

    public static <PageObjectClass extends BasePage> PageObjectClass expect(Class<PageObjectClass> pageClass) {
        return verify(initialize(pageClass));
    }

    public static <PageObjectClass extends BasePage> PageObjectClass initialize(Class<PageObjectClass> pageObjectClass) {
        try {
            Constructor<PageObjectClass> constructor = pageObjectClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance of " + pageObjectClass, e);
        }
    }

    protected static <PageObjectClass extends BasePage> PageObjectClass verify(PageObjectClass page) {
        if (page.isValid()) {
            return page;
        } else {
            throw new IllegalStateException("Page wasn't initialized");
        }
    }

    protected abstract String createUrl();
    protected abstract boolean isValid();
}

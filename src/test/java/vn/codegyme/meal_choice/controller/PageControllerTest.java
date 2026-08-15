package vn.codegyme.meal_choice.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageControllerTest {

    @Test
    void rootPageShouldOpenRegisterTabByDefault() {
        PageController controller = new PageController(null, null);

        assertEquals("redirect:/login?tab=register", controller.homePage());
    }
}

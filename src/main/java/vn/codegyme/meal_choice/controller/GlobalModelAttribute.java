package vn.codegyme.meal_choice.controller;

import com.truanayangi.config.AppProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;

@ControllerAdvice
public class GlobalModelAttribute {

    private final AppProperties appProperties;

    public GlobalModelAttribute(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @ModelAttribute("appInfo")
    public AppProperties appInfo() {
        return appProperties;
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return Year.now().getValue();
    }
}

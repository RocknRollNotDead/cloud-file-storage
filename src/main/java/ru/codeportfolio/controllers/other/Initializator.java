package ru.codeportfolio.controllers.other;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class Initializator implements WebApplicationInitializer {

    private static final String DISPATCHER = "dispatcher";

    @Override
    public void onStartup(ServletContext servletContext){

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppRootConfig.class);

        AnnotationConfigWebApplicationContext controllerContext = new AnnotationConfigWebApplicationContext();
        controllerContext.register(ControllerConfig.class);
        controllerContext.setParent(rootContext);

        servletContext.addListener(new ContextLoaderListener(rootContext));

        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(
                        DISPATCHER, new DispatcherServlet(controllerContext));

        servletRegistration.addMapping("/api/");
        servletRegistration.setLoadOnStartup(1);


    }
}

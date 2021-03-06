/**
 * Copyright 2009-2013 Oy Vaadin Ltd
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yarlithub;


import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import org.yarlithub.login.SimpleLoginUI;
import org.yarlithub.ui.MainAppView;

import javax.servlet.annotation.WebServlet;

@Title("Yarl IT Hub - Talent Database")
@Theme("mytheme")
@Widgetset("org.yarlithub.MyAppWidgetset")
public class RootUI extends UI {

    public static final String PERSISTENCE_UNIT = "yarlithub";

    @Override
    protected void init(VaadinRequest request) {

        setContent(new MainAppView());

    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = SimpleLoginUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {

    }
}

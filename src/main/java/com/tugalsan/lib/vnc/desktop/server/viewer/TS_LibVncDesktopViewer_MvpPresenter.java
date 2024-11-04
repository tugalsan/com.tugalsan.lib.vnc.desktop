// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package com.tugalsan.lib.vnc.desktop.server.viewer;

import com.tugalsan.lib.vnc.desktop.server.viewer.TS_LibVncDesktopViewer_Viewer;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Presenter layer for Model-View-Presenter architecture
 *
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopViewer_MvpPresenter {

    private final Map<String, TS_LibVncDesktopViewer_MvpView> registeredViews;
    private final Map<String, TS_LibVncDesktopViewer_MvpModel> registeredModels;
    private static final Logger logger = Logger.getLogger(TS_LibVncDesktopViewer_MvpPresenter.class.getName());
    private Throwable savedInvocationTargetException;
    public TS_LibVncDesktopViewer_Viewer viewer;

    public TS_LibVncDesktopViewer_MvpPresenter(TS_LibVncDesktopViewer_Viewer viewer) {
        this.viewer = viewer;
        registeredViews = new HashMap();
        registeredModels = new HashMap();
    }

    /**
     * Register View at the Presenter
     *
     * @param name - name (id) of View
     * @param view - the View
     */
    public void addView(String name, TS_LibVncDesktopViewer_MvpView view) {
        registeredViews.put(name, view);
    }

    public Set<Map.Entry<String, TS_LibVncDesktopViewer_MvpView>> removeAllViews() {
        var save = registeredViews.entrySet();
        registeredViews.clear();
        return save;
    }

    /**
     * Register Model at the Presenter
     *
     * @param name - name (id) of Model
     * @param model - the Model
     */
    public void addModel(String name, TS_LibVncDesktopViewer_MvpModel model) {
        registeredModels.put(name, model);
    }

    /**
     * Iterate over Models and pass available model properties of each model
     * into Views
     *
     * @see #populateFrom(String) for more details
     */
    protected void populate() {
        savedInvocationTargetException = null;
        registeredModels.entrySet().forEach(entry -> {
            var modelName = entry.getKey();
            var model = entry.getValue();
            populateFrom(modelName, model);
        });
    }

    /**
     * Iterate over model's getters, gets model property (name, value and its
     * value type), then tries to set view's property with the same name and
     * value type. Skip property getting or setting on any access errors
     *
     * @param modelName name of Model
     */
    public void populateFrom(String modelName) {
        var model = registeredModels.get(modelName);
        if (model != null) {
            populateFrom(modelName, model);
        } else {
            logger.log(Level.FINER, "Cannot find model: {0}", modelName);
        }
    }

    private void populateFrom(String modelName, TS_LibVncDesktopViewer_MvpModel model) {
        var methods = model.getClass().getDeclaredMethods();
        for (var m : methods) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                var propertyName = m.getName().substring(3);
                try {
                    var property = m.invoke(model);
                    logger.log(Level.FINEST, "Load: {0}.get{1}() # => {2}  type: {3}", new Object[]{modelName, propertyName, property, m.getReturnType()});
                    setViewProperty(propertyName, property, m.getReturnType()); // TODO this can set savedInvocationTargetEx, so what to do whith it?
                } catch (IllegalAccessException ignore) {
                    // nop
                } catch (InvocationTargetException e) {
                    savedInvocationTargetException = e.getCause(); // TODO may be skip it?
                    break;
                }
            }
        }
    }

    protected boolean isModelRegisteredByName(String modelName) {
        return registeredModels.containsKey(modelName);
    }

    protected TS_LibVncDesktopViewer_MvpModel getModel(String modelName) {
        return registeredModels.get(modelName);
    }

    protected void show() {
        registeredViews.values().forEach(v -> {
            v.showView();
        });
    }

    protected void save() {
        savedInvocationTargetException = null;
        for (var entry : registeredModels.entrySet()) {
            var modelName = entry.getKey();
            var model = entry.getValue();
            var methods = model.getClass().getDeclaredMethods();
            for (var m : methods) {
                if (m.getName().startsWith("set")) {
                    var propertyName = m.getName().substring(3);
                    try {
                        var viewProperty = getViewProperty(propertyName);
                        m.invoke(model, viewProperty);
                        logger.log(Level.FINEST, "Save: {0}.set{1}( {2} )", new Object[]{modelName, propertyName, viewProperty});
                    } catch (IllegalAccessException | TS_LibVncDesktopViewer_MvpPropertyNotFoundException ignore) {
                        // nop
                    } catch (InvocationTargetException e) {
                        savedInvocationTargetException = e.getCause();
                        break;
                    }
                    // nop

                }
            }
        }
    }

    public Object getViewPropertyOrNull(String propertyName) {
        try {
            return getViewProperty(propertyName);
        } catch (TS_LibVncDesktopViewer_MvpPropertyNotFoundException e) {
            return null;
        }
    }

    public Object getViewProperty(String propertyName) throws TS_LibVncDesktopViewer_MvpPropertyNotFoundException {
        savedInvocationTargetException = null;
        logger.log(Level.FINEST, "get{0}()", propertyName);
        for (var entry : registeredViews.entrySet()) {
            var viewName = entry.getKey();
            var view = entry.getValue();
            try {
                var getter = view.getClass().getMethod("get" + propertyName, new Class[0]);
                var res = getter.invoke(view);
                logger.log(Level.FINEST, "----from view: {0}.get{1}() # +> {2}", new Object[]{viewName, propertyName, res});
                return res;
                // oops, only first getter will be found TODO?
            } catch (NoSuchMethodException | IllegalAccessException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            }
            // nop

        }
        throw new TS_LibVncDesktopViewer_MvpPropertyNotFoundException(propertyName);
    }

    public Object getModelProperty(String propertyName) {
        savedInvocationTargetException = null;
        logger.log(Level.FINEST, "get{0}()", propertyName);
        for (var modelName : registeredModels.keySet()) {
            var model = registeredModels.get(modelName);
            try {
                var getter = model.getClass().getMethod("get" + propertyName, new Class[0]);
                var res = getter.invoke(model);
                logger.log(Level.FINEST, "----from model: {0}.get{1}() # +> {2}", new Object[]{modelName, propertyName, res});
                return res;
                // oops, only first getter will be found TODO?
            } catch (NoSuchMethodException | IllegalAccessException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            }
            // nop

        }
//        savedInvocationTargetException = new PropertyNotFoundException(propertyName);
        return null;
    }

    public void setViewProperty(String propertyName, Object newValue) {
        setViewProperty(propertyName, newValue, newValue.getClass());
    }

    public void setViewProperty(String propertyName, Object newValue, Class<?> valueType) {
        savedInvocationTargetException = null;
        logger.log(Level.FINEST, "set{0}( {1} ) type: {2}", new Object[]{propertyName, newValue, valueType});
        for (var entry : registeredViews.entrySet()) {
            var viewName = entry.getKey();
            var view = entry.getValue();
            try {
                var setter = view.getClass().getMethod("set" + propertyName, valueType);
                setter.invoke(view, newValue);
                logger.log(Level.FINEST, "----to view: {0}.set{1}( {2} )", new Object[]{viewName, propertyName, newValue});
            } catch (NoSuchMethodException | IllegalAccessException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                logger.info("WARNING: @setViewProperty -> %s".formatted(e.getCause().getMessage()));
                savedInvocationTargetException = e.getCause();
                break;
            }
            // nop

        }
    }

    protected void throwPossiblyHappenedException() throws Throwable {
        if (savedInvocationTargetException != null) {
            var tmp = savedInvocationTargetException;
            savedInvocationTargetException = null;
            throw tmp;
        }
    }

    protected TS_LibVncDesktopViewer_MvpView getView(String name) {
        return registeredViews.get(name);
    }

    public void setModelProperty(String propertyName, Object newValue) {
        setModelProperty(propertyName, newValue, newValue.getClass());
    }

    public void setModelProperty(String propertyName, Object newValue, Class<?> valueType) {
        savedInvocationTargetException = null;
        logger.log(Level.FINEST, "set{0}( {1} )", new Object[]{propertyName, newValue});
        for (var entry : registeredModels.entrySet()) {
            var modelName = entry.getKey();
            var model = entry.getValue();
            try {
                var method = model.getClass().getMethod("set" + propertyName, valueType);
                method.invoke(model, newValue);
                logger.log(Level.FINEST, "----for model: {0}", modelName);
            } catch (NoSuchMethodException | IllegalAccessException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            }
            // nop

        }
    }
}

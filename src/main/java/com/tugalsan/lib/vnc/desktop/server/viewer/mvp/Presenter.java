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
package com.tugalsan.lib.vnc.desktop.server.viewer.mvp;

import com.tugalsan.lib.vnc.desktop.server.viewer.Viewer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Presenter layer for Model-View-Presenter architecture
 *
 * @author dime at tightvnc.com
 */
public class Presenter {

    private final Map<String, View> registeredViews;
    private final Map<String, Model> registeredModels;
    static private Logger logger = Logger.getLogger(Presenter.class.getName());
    private Throwable savedInvocationTargetException;
    public Viewer viewer;
    
    public Presenter(Viewer viewer) {
        this.viewer  = viewer;
        registeredViews = new HashMap<String, View>();
        registeredModels = new HashMap<String, Model>();
    }

    /**
     * Register View at the Presenter
     *
     * @param name - name (id) of View
     * @param view - the View
     */
    public void addView(String name, View view) {
        registeredViews.put(name, view);
    }

    public Set<Map.Entry<String, View>> removeAllViews() {
        final Set<Map.Entry<String, View>> save = registeredViews.entrySet();
        registeredViews.clear();
        return save;
    }

    /**
     * Register Model at the Presenter
     *
     * @param name - name (id) of Model
     * @param model - the Model
     */
    public void addModel(String name, Model model) {
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
        for (Map.Entry<String, Model> entry : registeredModels.entrySet()) {
            String modelName = entry.getKey();
            Model model = entry.getValue();
            populateFrom(modelName, model);
        }
    }

    /**
     * Iterate over model's getters, gets model property (name, value and its
     * value type), then tries to set view's property with the same name and
     * value type. Skip property getting or setting on any access errors
     *
     * @param modelName name of Model
     */
    public void populateFrom(String modelName) {
        Model model = registeredModels.get(modelName);
        if (model != null) {
            populateFrom(modelName, model);
        } else {
            logger.finer("Cannot find model: " + modelName);
        }
    }

    private void populateFrom(String modelName, Model model) {
        Method methods[] = model.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                String propertyName = m.getName().substring(3);
                try {
                    final Object property = m.invoke(model);
                    logger.finest("Load: " + modelName + ".get" + propertyName + "() # => " + property
                            + "  type: " + m.getReturnType());
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

    protected Model getModel(String modelName) {
        return registeredModels.get(modelName);
    }

    protected void show() {
        for (View v : registeredViews.values()) {
            v.showView();
        }
    }

    protected void save() {
        savedInvocationTargetException = null;
        for (Map.Entry<String, Model> entry : registeredModels.entrySet()) {
            String modelName = entry.getKey();
            Model model = entry.getValue();
            Method methods[] = model.getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().startsWith("set")) {
                    String propertyName = m.getName().substring(3);
                    try {
                        final Object viewProperty = getViewProperty(propertyName);
                        m.invoke(model, viewProperty);
                        logger.finest("Save: " + modelName + ".set" + propertyName + "( " + viewProperty + " )");
                    } catch (IllegalAccessException ignore) {
                        // nop
                    } catch (InvocationTargetException e) {
                        savedInvocationTargetException = e.getCause();
                        break;
                    } catch (PropertyNotFoundException e) {
                        // nop
                    }
                }
            }
        }
    }

    public Object getViewPropertyOrNull(String propertyName) {
        try {
            return getViewProperty(propertyName);
        } catch (PropertyNotFoundException e) {
            return null;
        }
    }

    public Object getViewProperty(String propertyName) throws PropertyNotFoundException {
        savedInvocationTargetException = null;
        logger.finest("get" + propertyName + "()");
        for (Map.Entry<String, View> entry : registeredViews.entrySet()) {
            String viewName = entry.getKey();
            View view = entry.getValue();
            try {
                Method getter = view.getClass().getMethod("get" + propertyName, new Class[0]);
                final Object res = getter.invoke(view);
                logger.finest("----from view: " + viewName + ".get" + propertyName + "() # +> " + res);
                return res;
                // oops, only first getter will be found TODO?
            } catch (NoSuchMethodException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            } catch (IllegalAccessException ignore) {
                // nop
            }
        }
        throw new PropertyNotFoundException(propertyName);
    }

    public Object getModelProperty(String propertyName) {
        savedInvocationTargetException = null;
        logger.finest("get" + propertyName + "()");
        for (String modelName : registeredModels.keySet()) {
            Model model = registeredModels.get(modelName);
            try {
                Method getter = model.getClass().getMethod("get" + propertyName, new Class[0]);
                final Object res = getter.invoke(model);
                logger.finest("----from model: " + modelName + ".get" + propertyName + "() # +> " + res);
                return res;
                // oops, only first getter will be found TODO?
            } catch (NoSuchMethodException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            } catch (IllegalAccessException ignore) {
                // nop
            }
        }
//        savedInvocationTargetException = new PropertyNotFoundException(propertyName);
        return null;
    }

    public void setViewProperty(String propertyName, Object newValue) {
        setViewProperty(propertyName, newValue, newValue.getClass());
    }

    public void setViewProperty(String propertyName, Object newValue, Class<?> valueType) {
        savedInvocationTargetException = null;
        logger.finest("set" + propertyName + "( " + newValue + " ) type: " + valueType);
        for (Map.Entry<String, View> entry : registeredViews.entrySet()) {
            String viewName = entry.getKey();
            View view = entry.getValue();
            try {
                Method setter = view.getClass().getMethod("set" + propertyName, valueType);
                setter.invoke(view, newValue);
                logger.finest("----to view: " + viewName + ".set" + propertyName + "( " + newValue + " )");
            } catch (NoSuchMethodException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                savedInvocationTargetException = e.getCause();
                break;
            } catch (IllegalAccessException ignore) {
                // nop
            }
        }
    }

    protected void throwPossiblyHappenedException() throws Throwable {
        if (savedInvocationTargetException != null) {
            Throwable tmp = savedInvocationTargetException;
            savedInvocationTargetException = null;
            throw tmp;
        }
    }

    protected View getView(String name) {
        return registeredViews.get(name);
    }

    public void setModelProperty(String propertyName, Object newValue) {
        setModelProperty(propertyName, newValue, newValue.getClass());
    }

    public void setModelProperty(String propertyName, Object newValue, Class<?> valueType) {
        savedInvocationTargetException = null;
        logger.finest("set" + propertyName + "( " + newValue + " )");
        for (Map.Entry<String, Model> entry : registeredModels.entrySet()) {
            String modelName = entry.getKey();
            Model model = entry.getValue();
            try {
                Method method = model.getClass().getMethod("set" + propertyName, valueType);
                method.invoke(model, newValue);
                logger.finest("----for model: " + modelName);
            } catch (NoSuchMethodException ignore) {
                // nop
            } catch (InvocationTargetException e) {
                savedInvocationTargetException = e.getCause();
                break;
            } catch (IllegalAccessException ignore) {
                // nop
            }
        }
    }
}

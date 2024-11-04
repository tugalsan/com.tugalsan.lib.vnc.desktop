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
package com.tugalsan.lib.vnc.desktop.server.base;

/**
 * @author dime at tightvnc.com
 */
public class TS_LibVncDesktopUtils_LazyLoaded<T> {

    private boolean isLoaded;
    private T lazyObj;
    private Loader<T> loader;

    private TS_LibVncDesktopUtils_LazyLoaded() {
    }

    public T get() {
        if (isLoaded) {
            return lazyObj;
        } else {
            try {
                lazyObj = loader.load();
                isLoaded = true;
            } catch (Throwable ignore) {
                return null;
            }
            return lazyObj;
        }
    }

    public TS_LibVncDesktopUtils_LazyLoaded(Loader<T> loader) {
        this.loader = loader;
    }

    public interface Loader<T> {

        /**
         * Loads the lazy loaded object
         *
         * @return object loaded
         */
        T load() throws Throwable;
    }
}

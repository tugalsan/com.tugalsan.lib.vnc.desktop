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

/**
 * View layer for Model-View-Presenter architecture
 *
 * The View is a visible and user interacted object. The Presenter layer of
 * architecture determines when to show the View and provide data flow between
 * views and models. The View must have a set of getters and setters for data
 * properties it represents.
 *
 * @author dime at tightvnc.com
 */
public interface View {

    /**
     * Make the view visible
     */
    void showView();

    /**
     * Close the view
     */
    void closeView();
}

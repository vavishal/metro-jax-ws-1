/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package epr.epr_extensions.common;

import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.BindingID;

/**
 * @author Rama.Pulavarthi@sun.com
 */
public class MyTubelineAssemblerFactory extends TubelineAssemblerFactory {
    public TubelineAssembler doCreate(BindingID bindingId) {
        return new MyTubelineAssembler();
    }
}

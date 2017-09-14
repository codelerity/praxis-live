/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 */
package net.neilcsmith.praxis.live.model;

import java.util.Optional;
import net.neilcsmith.praxis.core.ComponentAddress;
import net.neilcsmith.praxis.core.ComponentType;
import net.neilcsmith.praxis.core.info.ComponentInfo;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public interface ComponentProxy extends Proxy {

    public ComponentAddress getAddress();

    public ComponentType getType();

    public ComponentInfo getInfo();

    public ContainerProxy getParent();

    public static Optional<ComponentProxy> find(ComponentAddress address) {
        Optional<RootProxy> root = RootProxy.find(address.getRootID());
        if (!root.isPresent()) {
            return Optional.empty();
        } else if (address.getDepth() == 1) {
            return Optional.of(root.get());
        } else {
            ComponentProxy cmp = root.get();
            for (int i = 1; i < address.getDepth(); i++) {
                if (cmp instanceof ContainerProxy) {
                    cmp = ((ContainerProxy) cmp).getChild(address.getComponentID(i));
                } else {
                    return Optional.empty();
                }
            }
            return Optional.ofNullable(cmp);
        }

    }

}

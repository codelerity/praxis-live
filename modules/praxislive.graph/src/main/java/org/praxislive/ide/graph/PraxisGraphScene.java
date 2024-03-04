/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this work; if not, see http://www.gnu.org/licenses/
 *
 *
 * Linking this work statically or dynamically with other modules is making a
 * combined work based on this work. Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this work give you permission
 * to link this work with independent modules to produce an executable,
 * regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that
 * you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not
 * derived from or based on this work. If you modify this work, you may extend
 * this exception to your version of the work, but you are not obligated to do so.
 * If you do not wish to do so, delete this exception statement from your version.
 *
 * Please visit http://neilcsmith.net if you need additional information or
 * have any questions.
 *
 *
 * This class is derived from code in NetBeans Visual Library.
 * Original copyright notice follows.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.praxislive.ide.graph;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.swing.UIManager;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

public class PraxisGraphScene<N> extends GraphPinScene<N, EdgeID<N>, PinID<N>> {

    private final static double LOD_ZOOM = 0.7;
    
    private final LayerWidget backgroundLayer = new LayerWidget(this);
    private final LayerWidget mainLayer = new LayerWidget(this);
    private final LayerWidget connectionLayer = new LayerWidget(this);
    private final LayerWidget upperLayer = new LayerWidget(this);
    
    private final CommentWidget commentWidget;
    
    private boolean orthogonal;
    private Router router;
    private final WidgetAction moveAction;
    private final PraxisKeyboardMoveAction keyboardMoveAction;
    private final SceneLayout sceneLayout;
    private LAFScheme scheme;
    private WidgetAction menuAction;
    private WidgetAction connectAction;
    private LAFScheme.Colors schemeColors;

//    private int edgeCount = 10;
    /**
     * Creates a VMD graph scene.
     */
    public PraxisGraphScene() {
        this(null, null, null);
    }

    /**
     * Creates a VMD graph scene with a specific color scheme.
     *
     * @param scheme the color scheme
     */
    public PraxisGraphScene(LAFScheme scheme) {
        this(scheme, null, null);
    }

    public PraxisGraphScene(ConnectProvider connectProvider, PopupMenuProvider popupProvider) {
        this(null, connectProvider, popupProvider);
    }

    public PraxisGraphScene(LAFScheme scheme, ConnectProvider connectProvider, PopupMenuProvider popupProvider) {
        if (scheme == null) {
            scheme = new LAFScheme();
        }
        this.scheme = scheme;
        
        setFont(UIManager.getFont("controlFont"));

        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(upperLayer);
        
        PraxisMoveProvider mover = new PraxisMoveProvider(this, backgroundLayer);
        moveAction = ActionFactory.createMoveAction(mover, mover);
        keyboardMoveAction = new PraxisKeyboardMoveAction(mover, mover);
        
        commentWidget = new CommentWidget(this);
        commentWidget.setPreferredLocation(new Point(32,32));
        commentWidget.setBorder(BorderFactory.createRoundedBorder(8, 8, 8, 8, Color.LIGHT_GRAY, null));
        commentWidget.setVisible(false);
        mainLayer.addChild(commentWidget);

        setBackground(scheme.getBackgroundColor());

        router = RouterFactory.createDirectRouter();

        getActions().addAction(ActionFactory.createWheelPanAction());
        getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.2));
        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createCycleFocusAction(new PraxisCycleFocusProvider()));

        if (connectProvider != null) {
            connectAction = ActionFactory.createConnectAction(new PraxisConnectDecorator(), connectionLayer, connectProvider);
        }
        if (popupProvider != null) {
            menuAction = ActionFactory.createPopupMenuAction(popupProvider);
            getActions().addAction(menuAction);
        }
        
        getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        
        addSceneListener(new ZoomCorrector());

        sceneLayout = LayoutFactory.createSceneGraphLayout(this, new PraxisGraphLayout<>(true));
        
    }

    public NodeWidget addNode(N node, String name) {
        NodeWidget n = (NodeWidget) super.addNode(node);
        n.setNodeName(name);
        return n;
    }

    @Override
    protected void detachNodeWidget(N node, Widget widget) {
        ((NodeWidget) widget).getCommentWidget().removeFromParent();
        super.detachNodeWidget(node, widget);
    }

    public PinWidget addPin(N node, String name) {
        return addPin(new PinID<N>(node, name),
                Alignment.Center);
    }

    public PinWidget addPin(N node, String name, Alignment alignment) {
        return addPin(new PinID<N>(node, name), alignment);
    }

    public PinWidget addPin(PinID<N> pin, Alignment alignment) {
        if (pin == null || alignment == null) {
            throw new NullPointerException();
        }
        PinWidget p = (PinWidget) super.addPin(pin.getParent(), pin);
        p.setAlignment(alignment);
        return p;
    }

    public EdgeWidget connect(N node1, String pin1, N node2, String pin2) {
        return connect(new PinID<N>(node1, pin1),
                new PinID<N>(node2, pin2));
    }

    public EdgeWidget connect(PinID<N> p1, PinID<N> p2) {
        EdgeID<N> d = new EdgeID<N>(p1, p2);
        EdgeWidget e = (EdgeWidget) addEdge(d);
        setEdgeSource(d, p1);
        setEdgeTarget(d, p2);
        return e;
    }

    public void disconnect(N node1, String pin1, N node2, String pin2) {
        PinID<N> p1 = new PinID<N>(node1, pin1);
        PinID<N> p2 = new PinID<N>(node2, pin2);
        EdgeID<N> d = new EdgeID<N>(p1, p2);
        removeEdge(d);
    }

    public LAFScheme getLookAndFeel() {
        return scheme;
    }
    
    public void setSchemeColors(LAFScheme.Colors schemeColors) {
        this.schemeColors = schemeColors;
        revalidate();
    }
    
    public LAFScheme.Colors getSchemeColors() {
        return schemeColors;
    }

    public void setOrthogonalRouting(boolean orthogonal) {
        if (this.orthogonal != orthogonal) {
            this.orthogonal = orthogonal;
            setRouter(orthogonal ?
                    RouterFactory.createOrthogonalSearchRouter(mainLayer, upperLayer) :
                    RouterFactory.createDirectRouter());
        }
    }
    
    public boolean isOrthogonalRouting() {
        return orthogonal;
    }
    
    void setRouter(Router router) {
        this.router = router;
        for (EdgeID<N> e : getEdges()) {
            ((ConnectionWidget)findWidget(e)).setRouter(router);
        }
        revalidate();
    }
    
    Router getRouter() {
        return router;
    }

    @Override
    public void userSelectionSuggested(Set<?> suggestedSelectedObjects, boolean invertSelection) {

        if (suggestedSelectedObjects.size() == 1 && isPin(suggestedSelectedObjects.iterator().next())) {
            suggestedSelectedObjects = Collections.emptySet();
        } else if (!suggestedSelectedObjects.isEmpty()) {
            Set<Object> selection = new LinkedHashSet<Object>(suggestedSelectedObjects.size());
            for (Object obj : suggestedSelectedObjects) {
                if (isPin(obj)) {
                    continue;
                }
                selection.add(obj);
            }
            suggestedSelectedObjects = selection;
        }
        super.userSelectionSuggested(suggestedSelectedObjects, invertSelection);
    }
    
    

    /**
     * Implements attaching a widget to a node. The widget is NodeWidget and has
     * object-hover, select, popup-menu and move actions.
     *
     * @param node the node
     * @return the widget attached to the node
     */
    @Override
    protected Widget attachNodeWidget(N node) {
        NodeWidget widget = new NodeWidget(this);
        mainLayer.addChild(widget);

        widget.getHeader().getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(moveAction);
        widget.getActions().addAction(keyboardMoveAction);
        if (menuAction != null) {
            widget.getActions().addAction(menuAction);
        }
        return widget;
    }

    /**
     * Implements attaching a widget to a pin.
     *
     * @param node the node
     * @param pin the pin
     * @return the widget attached to the pin
     */
    @Override
    protected Widget attachPinWidget(N node, PinID<N> pin) {
        NodeWidget nodeWidget = (NodeWidget) findWidget(node);
        PinWidget widget = new PinWidget(this, nodeWidget, pin.getName());
        nodeWidget.attachPinWidget(widget);
        widget.getActions().addAction(createObjectHoverAction());
        if (connectAction != null) {
            widget.getActions().addAction(connectAction);
        }
        if (menuAction != null) {
            widget.getActions().addAction(menuAction);
        }
        return widget;
    }

    /**
     * Implements attaching a widget to an edge.
     *
     * @param edge the edge
     * @return the widget attached to the edge
     */
    @Override
    protected Widget attachEdgeWidget(final EdgeID<N> edge) {
        PinWidget src = (PinWidget) findWidget(edge.getPin1());
        PinWidget dst = (PinWidget) findWidget(edge.getPin2());
        EdgeWidget edgeWidget = new EdgeWidget(this, src, dst);
        edgeWidget.setRouter(router);
        connectionLayer.addChild(edgeWidget);
        edgeWidget.getActions().addAction(createObjectHoverAction());
        edgeWidget.getActions().addAction(createSelectAction());
        if (menuAction != null) {
            edgeWidget.getActions().addAction(menuAction);
        }
        return edgeWidget;
    }

    /**
     * Attaches an anchor of a source pin an edge. The anchor is a ProxyAnchor
     * that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of
     * the node.
     *
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    @Override
    protected void attachEdgeSourceAnchor(EdgeID<N> edge, PinID<N> oldSourcePin, PinID<N> sourcePin) {
        ((EdgeWidget) findWidget(edge)).setSourceAnchor(getPinAnchor(sourcePin));
    }

    /**
     * Attaches an anchor of a target pin an edge. The anchor is a ProxyAnchor
     * that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of
     * the node.
     *
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    @Override
    protected void attachEdgeTargetAnchor(EdgeID<N> edge, PinID<N> oldTargetPin, PinID<N> targetPin) {
        ((EdgeWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }

    private Anchor getPinAnchor(PinID<N> pin) {
        if (pin == null) {
            return null;
        }
        PinWidget p = (PinWidget) findWidget(pin);
        return p.createAnchor();
    }
    
    public boolean isBelowLODThreshold() {
        return getZoomFactor() < LOD_ZOOM;
    }
    
    public void setComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            // remove comment
            commentWidget.setText("");
            commentWidget.setVisible(false);
        } else {
            // add comment
            commentWidget.setText(comment);
            commentWidget.setVisible(true);
        }
    }

    public String getComment() {
        return commentWidget.getText();
    }
    
    public Widget getCommentWidget() {
        return commentWidget;
    }
    
    public void layoutScene() {
        sceneLayout.invokeLayout();
    }  
    
    private class ZoomCorrector implements SceneListener {
        
        private final double minZoom = 0.2;
        private final double maxZoom = 2;

        @Override
        public void sceneRepaint() {
            // no op
        }

        @Override
        public void sceneValidating() {
            double zoom = getZoomFactor();
            if (zoom < minZoom) {
                setZoomFactor(minZoom);
            } else if (zoom > maxZoom) {
                setZoomFactor(maxZoom);
            }
        }

        @Override
        public void sceneValidated() {
            // no op
        }
        
    }

    
    
    private static class WidgetCollector implements ConnectionWidgetCollisionsCollector {

        @Override
        public void collectCollisions(ConnectionWidget connectionWidget, List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions) {
            // anchor widget is pin - get node.
            Widget w1 = connectionWidget.getSourceAnchor().getRelatedWidget().getParentWidget();
            Widget w2 = connectionWidget.getTargetAnchor().getRelatedWidget().getParentWidget();
            Rectangle rect;

            rect = w1.getBounds();
            rect = w1.convertLocalToScene(rect);
            rect.grow(10, 10);
            verticalCollisions.add(rect);
            horizontalCollisions.add(rect);

            rect = w2.getBounds();
            rect = w2.convertLocalToScene(rect);
            rect.grow(10, 10);
            verticalCollisions.add(rect);
            horizontalCollisions.add(rect);
        }
    }
}

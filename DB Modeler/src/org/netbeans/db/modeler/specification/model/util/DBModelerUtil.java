/**
 * Copyright [2016] Gaurav Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.netbeans.db.modeler.specification.model.util;

import java.awt.Image;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.eclipse.persistence.exceptions.ValidationException;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor.Mode;
import org.eclipse.persistence.internal.jpa.metadata.xml.DBEntityMappings;
import org.eclipse.persistence.internal.jpa.metadata.xml.XMLEntityMappings;
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.tools.schemaframework.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.db.modeler.classloader.DynamicDriverClassLoader;
import org.netbeans.db.modeler.core.widget.BaseTableWidget;
import org.netbeans.db.modeler.core.widget.CollectionTableWidget;
import org.netbeans.db.modeler.core.widget.ColumnWidget;
import org.netbeans.db.modeler.core.widget.ForeignKeyWidget;
import org.netbeans.db.modeler.core.widget.PrimaryKeyWidget;
import org.netbeans.db.modeler.core.widget.ReferenceFlowWidget;
import org.netbeans.db.modeler.core.widget.RelationTableWidget;
import org.netbeans.db.modeler.core.widget.TableWidget;
import org.netbeans.db.modeler.persistence.internal.jpa.deployment.JPAMPersistenceUnitProcessor;
import org.netbeans.db.modeler.persistence.internal.jpa.metadata.JPAMMetadataProcessor;
import org.netbeans.db.modeler.spec.DBColumn;
import org.netbeans.db.modeler.spec.DBMapping;
import org.netbeans.db.modeler.spec.DBTable;
import org.netbeans.db.modeler.specification.model.scene.DBModelerScene;
import org.netbeans.jpa.modeler.spec.EntityMappings;
import org.netbeans.jpa.modeler.spec.design.Bounds;
import org.netbeans.jpa.modeler.spec.design.Diagram;
import org.netbeans.jpa.modeler.spec.design.DiagramElement;
import org.netbeans.jpa.modeler.spec.design.Edge;
import org.netbeans.jpa.modeler.spec.design.Shape;
import org.netbeans.jpa.modeler.spec.extend.FlowNode;
import org.netbeans.jpa.modeler.spec.extend.cache.DatabaseConnectionCache;
import org.netbeans.modeler.anchors.CustomRectangularAnchor;
import org.netbeans.modeler.border.ResizeBorder;
import org.netbeans.modeler.config.document.IModelerDocument;
import org.netbeans.modeler.config.document.ModelerDocumentFactory;
import org.netbeans.modeler.core.ModelerFile;
import org.netbeans.modeler.core.NBModelerUtil;
import org.netbeans.modeler.core.exception.InvalidElmentException;
import org.netbeans.modeler.core.exception.ModelerException;
import org.netbeans.modeler.shape.ShapeDesign;
import org.netbeans.modeler.specification.model.ModelerDiagramSpecification;
import org.netbeans.modeler.specification.model.document.core.IFlowNode;
import org.netbeans.modeler.specification.model.document.widget.IFlowEdgeWidget;
import org.netbeans.modeler.specification.model.document.widget.IFlowNodeWidget;
import org.netbeans.modeler.specification.model.util.PModelerUtil;
import org.netbeans.modeler.widget.edge.IEdgeWidget;
import org.netbeans.modeler.widget.edge.info.EdgeWidgetInfo;
import org.netbeans.modeler.widget.node.INodeWidget;
import org.netbeans.modeler.widget.node.IPNodeWidget;
import org.netbeans.modeler.widget.node.NodeWidget;
import org.netbeans.modeler.widget.node.info.NodeWidgetInfo;
import org.netbeans.modeler.widget.node.vmd.PNodeWidget;
import org.netbeans.modeler.widget.pin.IPinWidget;
import org.netbeans.modeler.widget.pin.info.PinWidgetInfo;
import org.openide.*;
import org.openide.util.Exceptions;

public class DBModelerUtil implements PModelerUtil<DBModelerScene> {

    public static Image COLUMN;
    public static Image FOREIGNKEY;
    public static Image PRIMARYKEY;
    public static Image TAB_ICON;
    public static ImageIcon RELOAD_ICON;

    @Override
    public void init() {
        if (COLUMN == null) {
            ClassLoader cl = DBModelerUtil.class.getClassLoader();
            COLUMN = new ImageIcon(cl.getResource("org/netbeans/db/modeler/resource/image/column.gif")).getImage();
            FOREIGNKEY = new ImageIcon(cl.getResource("org/netbeans/db/modeler/resource/image/foreignkey.gif")).getImage();
            PRIMARYKEY = new ImageIcon(cl.getResource("org/netbeans/db/modeler/resource/image/primarykey.gif")).getImage();
            TAB_ICON = new ImageIcon(cl.getResource("org/netbeans/db/modeler/resource/image/tab_icon.png")).getImage();
            RELOAD_ICON = new ImageIcon(cl.getResource("org/netbeans/db/modeler/resource/image/reload.png"));
        }

    }

    @Override
    public void loadModelerFile(ModelerFile file) {
        try {

            EntityMappings entityMapping = (EntityMappings) file.getParentFile().getModelerScene().getBaseElementSpec();

            DBModelerScene scene = (DBModelerScene) file.getModelerScene();
            DBMapping dbMapping = createDBMapping(entityMapping);
            scene.setBaseElementSpec(dbMapping);

            ModelerDiagramSpecification modelerDiagram = file.getModelerDiagramModel();
            modelerDiagram.setDefinitionElement(entityMapping);

            dbMapping.getTables().stream().forEach(table -> loadTable(scene, (Widget) scene, table));
            loadFlowEdge(scene);
            scene.autoLayout();

        } catch (ValidationException ex) {
            file.getModelerPanelTopComponent().close();
            Logger.getLogger(DBModelerUtil.class.getName()).log(Level.INFO, null, ex);
            String message = ex.getLocalizedMessage();
            int end = message.lastIndexOf("Runtime Exceptions:");
            if (end < 1) {
                end = message.length();
            }
            int start = message.lastIndexOf("Exception Description:");
            if (start < 1) {
                start = 0;
            }
            message = message.substring(start, end);
            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (Exception ex) {
//            IO.getOut().println("Exception: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private DBMapping createDBMapping(EntityMappings entityMapping) {
        DBMapping dbMapping = new DBMapping();
        DatabaseConnectionCache connection = entityMapping.getCache().getDatabaseConnection();

        ClassLoader dynamicClassLoader;

        DatabaseLogin databaseLogin = new DatabaseLogin();
        ClassLoader contextClassLoader = null;
        if (connection == null) {
            dynamicClassLoader = new DynamicDriverClassLoader();
            databaseLogin.setDatabaseURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            databaseLogin.setUserName("");
            databaseLogin.setPassword("");
            databaseLogin.setDriverClass(org.h2.Driver.class);
        } else {
            dynamicClassLoader = new DynamicDriverClassLoader(connection.getDriverClass());//,connection.getDriverClass());
            contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(dynamicClassLoader);
            databaseLogin.setDatabaseURL(connection.getUrl());
            databaseLogin.setUserName(connection.getUserName());
            databaseLogin.setPassword(connection.getPassword());
            databaseLogin.setDriverClass(connection.getDriverClass());
        }
        DatabaseSessionImpl session = new DatabaseSessionImpl(databaseLogin);
        JPAMMetadataProcessor processor = new JPAMMetadataProcessor(session, dynamicClassLoader, true, false, true, true, false, null, null);
        XMLEntityMappings mapping = new DBEntityMappings(entityMapping);
        JPAMPersistenceUnitProcessor.processORMetadata(mapping, processor, true, Mode.ALL);

        processor.setClassLoader(dynamicClassLoader);
        processor.createDynamicClasses();
        processor.createRestInterfaces();
        processor.addEntityListeners();
        session.getProject().convertClassNamesToClasses(dynamicClassLoader);
        processor.processCustomizers();
        session.loginAndDetectDatasource();

        JPAMSchemaManager mgr = new JPAMSchemaManager(dbMapping, session);
        mgr.createDefaultTables(true);

        session.logout();

        if (connection != null) {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return dbMapping;
    }

//        
//           private  void loadFields(Entity entity , JPADynamicTypeBuilder entityBuilder) {
//
//               entity.getAttributes().getId().stream().forEach(a -> {
//                   String column = a.getColumn() != null ? a.getColumn().getName() : a.getName();
////                   try {
//                       entityBuilder.setPrimaryKeyFields(column);
////                       System.out.println("attr" + a.getAttributeType());
////                       String attr = a.getAttributeType();
////                       if(attr.indexOf('.') == -1){
////                           String primitive = attr.substring(0, 1).toUpperCase()+ attr.substring(1);
////                           if(isPrimitive(primitive)){
////                               attr = "java.lang." + primitive;
////                           }
////                       }
////                        Class.forName(attr)
//                       entityBuilder.addDirectMapping(a.getName(),String.class, column);
////                   } catch (ClassNotFoundException ex) {
////                       Exceptions.printStackTrace(ex);
////                   }
//
//               });
//                       
//
//               
//               entity.getAttributes().getBasic().stream().forEach(a -> {
//                   String column = a.getColumn() != null ? a.getColumn().getName() : a.getName();
////                   try {
////                       Class.forName(a.getAttributeType())
//                       entityBuilder.addDirectMapping(a.getName(), String.class, column);
////                   } catch (ClassNotFoundException ex) {
////                       Exceptions.printStackTrace(ex);
////                   }
//
//               });
//     
//    }
//        
//        
//         public static boolean isPrimitive(String type) {
//        return "boolean".equals(type) || "byte".equals(type)
//                || "short".equals(type) || "char".equals(type)
//                || "int".equals(type) || "long".equals(type)
//                || "float".equals(type) || "double".equals(type);
//    }
//        
    private void loadTable(DBModelerScene scene, Widget parentWidget, IFlowNode flowElement) {
        IModelerDocument document = null;
        ModelerDocumentFactory modelerDocumentFactory = scene.getModelerFile().getVendorSpecification().getModelerDocumentFactory();
        if (flowElement instanceof FlowNode) {
            FlowNode flowNode = (FlowNode) flowElement;

            try {
                document = modelerDocumentFactory.getModelerDocument(flowElement);
            } catch (ModelerException ex) {
                Exceptions.printStackTrace(ex);
            }
//            SubCategoryNodeConfig subCategoryNodeConfig = scene.getModelerFile().getVendorSpecification().getPaletteConfig().findSubCategoryNodeConfig(document);
            NodeWidgetInfo nodeWidgetInfo = new NodeWidgetInfo(flowElement.getId(), document, new Point(0, 0));
            nodeWidgetInfo.setName(flowElement.getName());
            nodeWidgetInfo.setExist(Boolean.TRUE);//to Load JPA
            nodeWidgetInfo.setBaseElementSpec(flowElement);//to Load JPA
            INodeWidget nodeWidget = scene.createNodeWidget(nodeWidgetInfo);
            if (flowElement.getName() != null) {
                nodeWidget.setLabel(flowElement.getName());
            }
            if (flowNode.isMinimized()) {
                ((PNodeWidget) nodeWidget).setMinimized(true);
            }
            if (flowElement instanceof DBTable) {
                DBTable table = (DBTable) flowElement;
                TableWidget tableWidget = (TableWidget) nodeWidget;
                if (table.getColumns() != null) {
                    table.getColumns().stream().forEach((column) -> {
                        if (column.isForeignKey()) {
                            tableWidget.addNewForeignKey(column.getName(), column);
                        } else if (column.isPrimaryKey()) {
                            tableWidget.addNewPrimaryKey(column.getName(), column);
                        } else {
                            tableWidget.addNewColumn(column.getName(), column);
                        }
                    });
                    tableWidget.sortAttributes();
                }

            }

        }
    }

    private void loadFlowEdge(DBModelerScene scene) {

        scene.getBaseElements().stream().filter((baseElementWidget) -> (baseElementWidget instanceof TableWidget)).forEach((baseElementWidget) -> {
            TableWidget tableWidget = (TableWidget) baseElementWidget;

            tableWidget.getForeignKeyWidgets().stream().forEach((foreignKeyWidget) -> {
                loadEdge(scene, tableWidget, (ForeignKeyWidget) foreignKeyWidget);
            });

        });
    }

    private void loadEdge(DBModelerScene scene, TableWidget sourceTableWidget, ForeignKeyWidget foreignKeyWidget) {
//       ForeignKey => Source
//       ReferenceColumn => Target      
        DBColumn sourceColumn = foreignKeyWidget.getBaseElementSpec();
        TableWidget targetTableWidget = (TableWidget) scene.getBaseElement(sourceColumn.getReferenceTable().getId());
        ColumnWidget targetColumnWidget = targetTableWidget.getPrimaryKeyWidget(sourceColumn.getReferenceColumn().getId());
        if (targetColumnWidget == null) {
            targetColumnWidget = targetTableWidget.getColumnWidget(sourceColumn.getReferenceColumn().getId());
        }

        EdgeWidgetInfo edgeInfo = new EdgeWidgetInfo();
        edgeInfo.setId(NBModelerUtil.getAutoGeneratedStringId());
        edgeInfo.setSource(sourceTableWidget.getNodeWidgetInfo().getId());
        edgeInfo.setTarget(targetTableWidget.getNodeWidgetInfo().getId());
//      edgeInfo.setType(NBModelerUtil.getEdgeType(sourceTableWidget, targetTableWidget, contextToolId));
        IEdgeWidget edgeWidget = scene.createEdgeWidget(edgeInfo);

        scene.setEdgeWidgetSource(edgeInfo, getEdgeSourcePinWidget(sourceTableWidget, targetTableWidget, edgeWidget, foreignKeyWidget));
        scene.setEdgeWidgetTarget(edgeInfo, getEdgeTargetPinWidget(sourceTableWidget, targetTableWidget, edgeWidget, targetColumnWidget));

    }

    private void loadDiagram(DBModelerScene scene, Diagram diagram, DiagramElement diagramElement) {
        if (diagramElement instanceof Shape) {
            Shape shape = (Shape) diagramElement;
            Bounds bounds = shape.getBounds();
            Widget widget = (Widget) scene.getBaseElement(shape.getElementRef());
            if (widget != null) {
                if (widget instanceof INodeWidget) { //reverse ref
                    INodeWidget nodeWidget = (INodeWidget) widget;
//                  nodeWidget.setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));
                    Point location = new Point((int) bounds.getX(), (int) bounds.getY());
                    nodeWidget.setPreferredLocation(location);
//                    nodeWidget.setActiveStatus(false);//Active Status is used to prevent reloading SVGDocument until complete document is loaded
//                    nodeWidget.setActiveStatus(true);
                } else {
                    throw new InvalidElmentException("Invalid JPA Element : " + widget);
                }
            }
        } else if (diagramElement instanceof Edge) {
//            JPAEdge edge = (JPAEdge) diagramElement;
//            Widget widget = (Widget) scene.getBaseElement(edge.getJPAElement());
//            if (widget != null && widget instanceof EdgeWidget) {
//                if (widget instanceof SequenceFlowWidget) {
//                    SequenceFlowWidget sequenceFlowWidget = (SequenceFlowWidget) widget;
//                    sequenceFlowWidget.setControlPoints(edge.getWaypointCollection(), true);
//                    if (edge.getJPALabel() != null) {
//                        Bounds bound = edge.getJPALabel().getBounds();
////                        sequenceFlowWidget.getLabelManager().getLabelWidget().getParentWidget().setPreferredLocation(bound.toPoint());
//                        sequenceFlowWidget.getLabelManager().getLabelWidget().getParentWidget().setPreferredLocation(
//                                sequenceFlowWidget.getLabelManager().getLabelWidget().convertSceneToLocal(bound.toPoint()));
//                    }
//                } else if (widget instanceof AssociationWidget) {
//                    AssociationWidget associationWidget = (AssociationWidget) widget;
//                    associationWidget.setControlPoints(edge.getWaypointCollection(), true);
//                } else {
//                    throw new InvalidElmentException("Invalid JPA Element");
//                }
////                EdgeWidget edgeWidget = (EdgeWidget)widget;
////                edgeWidget.manageControlPoint();
//
//            }
//
        }
    }

    @Override
    public void saveModelerFile(ModelerFile file) {
        file.getParentFile().getModelerUtil().saveModelerFile(file.getParentFile());
    }

    @Override
    public INodeWidget updateNodeWidgetDesign(ShapeDesign shapeDesign, INodeWidget inodeWidget) {
        PNodeWidget nodeWidget = (PNodeWidget) inodeWidget;
        //ELEMENT_UPGRADE
//        if (shapeDesign != null) {
//            if (shapeDesign.getOuterShapeContext() != null) {
//                if (shapeDesign.getOuterShapeContext().getBackground() != null) {
//                    nodeWidget.setOuterElementStartBackgroundColor(shapeDesign.getOuterShapeContext().getBackground().getStartColor());
//                    nodeWidget.setOuterElementEndBackgroundColor(shapeDesign.getOuterShapeContext().getBackground().getEndColor());
//                }
//                if (shapeDesign.getOuterShapeContext().getBorder() != null) {
//                    nodeWidget.setOuterElementBorderColor(shapeDesign.getOuterShapeContext().getBorder().getColor());
//                    nodeWidget.setOuterElementBorderWidth(shapeDesign.getOuterShapeContext().getBorder().getWidth());
//                }
//            }
//            if (shapeDesign.getInnerShapeContext() != null) {
//                if (shapeDesign.getInnerShapeContext().getBackground() != null) {
//                    nodeWidget.setInnerElementStartBackgroundColor(shapeDesign.getInnerShapeContext().getBackground().getStartColor());
//                    nodeWidget.setInnerElementEndBackgroundColor(shapeDesign.getInnerShapeContext().getBackground().getEndColor());
//                }
//                if (shapeDesign.getInnerShapeContext().getBorder() != null) {
//                    nodeWidget.setInnerElementBorderColor(shapeDesign.getInnerShapeContext().getBorder().getColor());
//                    nodeWidget.setInnerElementBorderWidth(shapeDesign.getInnerShapeContext().getBorder().getWidth());
//                }
//            }
//        }

        return (INodeWidget) nodeWidget;
    }

    @Override
    public Anchor getAnchor(INodeWidget inodeWidget) {
        INodeWidget nodeWidget = inodeWidget;
        Anchor sourceAnchor;
        if (nodeWidget instanceof IFlowNodeWidget) {
            sourceAnchor = new CustomRectangularAnchor(nodeWidget, 0, true);
        } else {
            throw new InvalidElmentException("Invalid JPA Process Element : " + nodeWidget);
        }
        return sourceAnchor;
    }

    @Override
    public void transformNode(IFlowNodeWidget flowNodeWidget, IModelerDocument document) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IPinWidget attachPinWidget(DBModelerScene scene, INodeWidget nodeWidget, PinWidgetInfo widgetInfo) {
        IPinWidget widget = null;
        if (widgetInfo.getDocumentId().equals(ColumnWidget.class.getSimpleName())) {
            widget = new ColumnWidget(scene, (IPNodeWidget) nodeWidget, widgetInfo);
        } else if (widgetInfo.getDocumentId().equals(ForeignKeyWidget.class.getSimpleName())) {
            widget = new ForeignKeyWidget(scene, (IPNodeWidget) nodeWidget, widgetInfo);
        } else if (widgetInfo.getDocumentId().equals(PrimaryKeyWidget.class.getSimpleName())) {
            widget = new PrimaryKeyWidget(scene, (IPNodeWidget) nodeWidget, widgetInfo);
        } else {
            throw new InvalidElmentException("Invalid DB Element");
        }
        return widget;
    }

    @Override
    public void dettachEdgeSourceAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, IPinWidget sourcePinWidget) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dettachEdgeTargetAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, IPinWidget targetPinWidget) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void attachEdgeSourceAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, IPinWidget sourcePinWidget) {
        edgeWidget.setSourceAnchor(scene.getPinAnchor(sourcePinWidget));

    }

    @Override
    public void attachEdgeSourceAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, INodeWidget sourceNodeWidget) { //BUG : Remove this method
        edgeWidget.setSourceAnchor(((IPNodeWidget) sourceNodeWidget).getNodeAnchor());
    }

    @Override
    public void attachEdgeTargetAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, IPinWidget targetPinWidget) {
        edgeWidget.setTargetAnchor(scene.getPinAnchor(targetPinWidget));
    }

    @Override
    public void attachEdgeTargetAnchor(DBModelerScene scene, IEdgeWidget edgeWidget, INodeWidget targetNodeWidget) { //BUG : Remove this method
        edgeWidget.setTargetAnchor(((IPNodeWidget) targetNodeWidget).getNodeAnchor());
    }

    @Override
    public IEdgeWidget attachEdgeWidget(DBModelerScene scene, EdgeWidgetInfo widgetInfo) {
        IEdgeWidget edgeWidget = getEdgeWidget(scene, widgetInfo);
        edgeWidget.setEndPointShape(PointShape.SQUARE_FILLED_SMALL);
        edgeWidget.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
        edgeWidget.setRouter(scene.getRouter());
        ((IFlowEdgeWidget) edgeWidget).setName(widgetInfo.getName());

        return edgeWidget;
    }

    @Override
    public ResizeBorder getNodeBorder(INodeWidget nodeWidget) {
        nodeWidget.setWidgetBorder(NodeWidget.RECTANGLE_RESIZE_BORDER);
        return PNodeWidget.RECTANGLE_RESIZE_BORDER;
    }

    @Override
    public INodeWidget attachNodeWidget(DBModelerScene scene, NodeWidgetInfo widgetInfo) {
        IFlowNodeWidget widget = null;
        IModelerDocument modelerDocument = widgetInfo.getModelerDocument();
        switch (modelerDocument.getId()) {
            case "BaseTable":
                widget = new BaseTableWidget(scene, widgetInfo);
                break;
            case "RelationTable":
                widget = new RelationTableWidget(scene, widgetInfo);
                break;
            case "CollectionTable":
                widget = new CollectionTableWidget(scene, widgetInfo);
                break;
            default:
                throw new InvalidElmentException("Invalid DB Element");
        }

        return (INodeWidget) widget;
    }

    private IEdgeWidget getEdgeWidget(DBModelerScene scene, EdgeWidgetInfo edgeWidgetInfo) {
        IEdgeWidget edgeWidget = new ReferenceFlowWidget(scene, edgeWidgetInfo);
        return edgeWidget;
    }

    @Override
    public String getEdgeType(INodeWidget sourceNodeWidget, INodeWidget targetNodeWidget, String connectionContextToolId) {
        String edgeType = connectionContextToolId;
        return edgeType;
    }

    @Override
    public PinWidgetInfo getEdgeSourcePinWidget(INodeWidget sourceNodeWidget, INodeWidget targetNodeWidget, IEdgeWidget edgeWidget) {
        return getEdgeSourcePinWidget(sourceNodeWidget, targetNodeWidget, edgeWidget, null);
    }

    public PinWidgetInfo getEdgeSourcePinWidget(INodeWidget sourceNodeWidget, INodeWidget targetNodeWidget, IEdgeWidget edgeWidget, ColumnWidget sourceColumnWidget) {
        if (sourceNodeWidget instanceof TableWidget && targetNodeWidget instanceof TableWidget && edgeWidget instanceof ReferenceFlowWidget && sourceColumnWidget instanceof ForeignKeyWidget) {
            ReferenceFlowWidget referenceFlowWidget = (ReferenceFlowWidget) edgeWidget;

//            TableWidget sourceTableWidget = (TableWidget) sourceNodeWidget;
//            Table sourceTable = (Table) sourceTableWidget.getBaseElementSpec();
            TableWidget targetTableWidget = (TableWidget) targetNodeWidget;
//            Table targetTable = (Table) targetTableWidget.getBaseElementSpec();

            DBColumn sourceColumn = sourceColumnWidget.getBaseElementSpec();
            ColumnWidget targetColumnWidget = targetTableWidget.getColumnWidget(sourceColumn.getReferenceColumn().getId());
//            Column targetColumn = targetColumnWidget.getBaseElementSpec();

            referenceFlowWidget.setReferenceColumnWidget(targetColumnWidget);
            referenceFlowWidget.setForeignKeyWidget((ForeignKeyWidget) sourceColumnWidget);
            return sourceColumnWidget.getPinWidgetInfo();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    @Override
    public PinWidgetInfo getEdgeTargetPinWidget(INodeWidget sourceNodeWidget, INodeWidget targetNodeWidget, IEdgeWidget edgeWidget) {
        return getEdgeTargetPinWidget(sourceNodeWidget, targetNodeWidget, edgeWidget, null);
    }

    public PinWidgetInfo getEdgeTargetPinWidget(INodeWidget sourceNodeWidget, INodeWidget targetNodeWidget, IEdgeWidget edgeWidget, ColumnWidget targetColumnWidget) {
        if (sourceNodeWidget instanceof TableWidget && targetNodeWidget instanceof TableWidget && edgeWidget instanceof ReferenceFlowWidget && targetColumnWidget instanceof ColumnWidget) {
            return targetColumnWidget.getPinWidgetInfo();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}

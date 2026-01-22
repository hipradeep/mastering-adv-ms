
import xml.etree.ElementTree as ET

def create_drawio_xml(filename):
    mxfile = ET.Element('mxfile', host="app.diagrams.net", modified="2024-05-20T00:00:00.000Z", agent="Version 1.0", etag="Wyr8...", version="24.0.0", type="device")
    diagram = ET.SubElement(mxfile, 'diagram', id="diagram_1", name="Page-1")
    mxGraphModel = ET.SubElement(diagram, 'mxGraphModel', dx="1422", dy="762", grid="1", gridSize="10", guides="1", tooltips="1", connect="1", arrows="1", fold="1", page="1", pageScale="1", pageWidth="827", pageHeight="1169", math="0", shadow="0")
    root = ET.SubElement(mxGraphModel, 'root')

    # Default layers
    ET.SubElement(root, 'mxCell', id="0")
    ET.SubElement(root, 'mxCell', id="1", parent="0")

    # Helper to add vertices
    def add_vertex(id, value, style, x, y, w, h, parent="1"):
        cell = ET.SubElement(root, 'mxCell', id=id, value=value, style=style, vertex="1", parent=parent)
        geometry = ET.SubElement(cell, 'mxGeometry', x=str(x), y=str(y), width=str(w), height=str(h))
        geometry.set('as', 'geometry')
        return cell

    # Helper to add containter (Group/Subgraph)
    def add_group(id, value, x, y, w, h):
        style = "group;verticalAlign=top;align=left;spacingTop=10;fontStyle=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;"
        return add_vertex(id, value, style, x, y, w, h)

    # Helper to add edges with labels
    def add_edge(id, source, target, label=""):
        style = "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;entryX=0.5;entryY=0;entryDx=0;entryDy=0;"
        cell = ET.SubElement(root, 'mxCell', id=id, value=label, style=style, edge="1", parent="1", source=source, target=target)
        geometry = ET.SubElement(cell, 'mxGeometry', relative="1")
        geometry.set('as', 'geometry')

    # Styles
    style_process = "rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;"
    style_db = "shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;fillColor=#fff2cc;strokeColor=#d6b656;"
    style_queue = "shape=parallelogram;perimeter=parallelogramPerimeter;whiteSpace=wrap;html=1;fixedSize=1;fillColor=#d5e8d4;strokeColor=#82b366;"
    
    # --- LAYERS (Visual Containers) ---
    # Width: 600, Center X approx 300
    
    # 1. Client Layer
    add_group("layer_client", "Client Layer", 40, 40, 600, 100)
    add_vertex("browser", "React Frontend", style_process, 260, 70, 160, 50, parent="1")

    # 2. Orchestration Layer
    add_group("layer_orch", "Orchestration Layer", 40, 160, 600, 120)
    add_vertex("orchestrator", "Orchestrator Service", style_process, 260, 200, 160, 60, parent="1")

    # 3. Messaging Layer
    add_group("layer_msg", "Messaging Layer", 40, 300, 600, 120)
    add_vertex("kafka", "Apache Kafka", style_queue, 240, 340, 200, 60, parent="1")

    # 4. Service Layer
    add_group("layer_svc", "Service Layer", 40, 440, 600, 120)
    add_vertex("inventory", "Inventory Service", style_process, 100, 480, 160, 60, parent="1")
    add_vertex("issue", "Issue Service", style_process, 420, 480, 160, 60, parent="1")

    # 5. Data Layer
    add_group("layer_data", "Data Layer", 40, 580, 600, 120)
    add_vertex("db_orch", "Orch DB", style_db, 460, 210, 60, 60, parent="1") # Placed near orchestrator logically, but visualy inside data layer? 
    # Actually, let's place DBs inside Data Layer as per graph
    add_vertex("db_orch_node", "Orch DB", style_db, 290, 620, 80, 60, parent="1") 
    add_vertex("db_inv_node", "Inventory DB", style_db, 140, 620, 80, 60, parent="1")
    add_vertex("db_issue_node", "Issue DB", style_db, 460, 620, 80, 60, parent="1")


    # --- CONNECTIONS ---
    
    # Browser -> Orch
    add_edge("e1", "browser", "orchestrator", "HTTP POST /issue")
    
    # Orch -> Kafka & DB
    add_edge("e2", "orchestrator", "kafka", "ReserveStockCommand")
    add_edge("e3", "orchestrator", "db_orch_node", "Persist Saga State")
    
    # Kafka -> Services
    add_edge("e4", "kafka", "inventory", "ReserveStockCommand")
    add_edge("e5", "kafka", "issue", "CreateIssueCommand")
    
    # Inventory Flows
    add_edge("e6", "inventory", "kafka", "StockReservedEvent") # Back to Kafka
    add_edge("e7", "inventory", "db_inv_node", "Atomic Native SQL Update")
    
    
    # Issue Flows
    add_edge("e8", "issue", "kafka", "IssueCreatedEvent") # Back to Kafka
    add_edge("e9", "issue", "db_issue_node", "Save Issue")
    
    # Failure Flow (Dashed/Dotted ideally, but standard edge for now with label)
    add_edge("e10", "issue", "kafka", "IssueFailedEvent (Rollback)")

    tree = ET.ElementTree(mxfile)
    tree.write(filename)
    print(f"Generated {filename}")

if __name__ == "__main__":
    create_drawio_xml("d:/Projects Workspaces/terminator/md_files/Architecture_Diagram.drawio")

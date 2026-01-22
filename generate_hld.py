from docx import Document
from docx.shared import Inches, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH

def create_hld():
    document = Document()

    # Title
    document.add_heading('High Level Design (HLD)', 0)
    p = document.add_paragraph('Terminator Project')
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER

    # Table of Contents (Placeholder logic as dynamic TOC is complex in python-docx)
    document.add_heading('Table of Contents', level=1)
    document.add_paragraph('1. System Overview')
    document.add_paragraph('2. High-Level Architecture')
    document.add_paragraph('3. Core Services')
    document.add_paragraph('4. Key Data Flows')
    document.add_paragraph('5. UI Design & Screenshots')
    document.add_page_break()

    # 1. System Overview
    document.add_heading('1. System Overview', level=1)
    document.add_paragraph(
        'The Terminator project is a distributed system designed to manage drug issuance to patients '
        'while maintaining real-time inventory accuracy. It addresses the critical need for consistency '
        'between physical stock and digital records in a high-concurrency hospital environment.'
    )
    document.add_heading('1.1 Key Features', level=2)
    p = document.add_paragraph(style='List Bullet')
    p.add_run('Distributed Transactions (Saga Pattern):').bold = True
    p.add_run(' Ensures data consistency across Orchestrator, Inventory, and Issue services.')
    
    p = document.add_paragraph(style='List Bullet')
    p.add_run('Real-time Stock Updates:').bold = True
    p.add_run(' Immediate reservation and deduction of stock to prevent overselling.')

    p = document.add_paragraph(style='List Bullet')
    p.add_run('Optimistic Locking:').bold = True
    p.add_run(' Prevents race conditions when multiple users access the same batch simultaneously.')

    # 2. High-Level Architecture
    document.add_heading('2. High-Level Architecture', level=1)
    document.add_paragraph('[Please Insert Architecture Diagram Here]')
    
    document.add_heading('2.1 Components', level=2)
    table = document.add_table(rows=1, cols=2)
    table.style = 'Table Grid'
    hdr_cells = table.rows[0].cells
    hdr_cells[0].text = 'Component'
    hdr_cells[1].text = 'Description'
    
    components = [
        ('Frontend', 'React.js Single Page Application acting as the user interface.'),
        ('API Gateway / Orchestrator', 'Central service managing transaction flows.'),
        ('Inventory Service', 'Manages drug stock, batches, and reservations.'),
        ('Issue Service', 'Handles patient transaction records.'),
        ('Apache Kafka', 'Message broker for asynchronous communication.'),
        ('PostgreSQL', 'Relational database (Service-per-DB pattern).'),
    ]
    
    for name, desc in components:
        row_cells = table.add_row().cells
        row_cells[0].text = name
        row_cells[1].text = desc

    # 3. Core Services
    document.add_heading('3. Core Services', level=1)
    
    document.add_heading('3.1 Orchestrator Service', level=2)
    document.add_paragraph(
        'Acts as the coordinator. It initiates the "Issue Drug" Saga, tracking the state of transactions '
        'from START to SUCCESS or FAILURE. It manages the compensations (rollbacks) if any step fails.'
    )

    document.add_heading('3.2 Inventory Service', level=2)
    document.add_paragraph(
        'Responsible for the integrity of stock data. It listens for reservation commands and updates '
        'the "HsttDrugCurrstockDtl" table. It uses **Atomic Native SQL Queries** (e.g., "UPDATE ... SET qty = qty - :x WHERE qty >= :x") '
        'to prevent race conditions without needing explicit locking.'
    )

    document.add_heading('3.3 Issue-to-Patient Service', level=2)
    document.add_paragraph(
        'Finalizes the transaction by creating a persistent record of the issue in the "HsttDrugIssueDtl" table.'
    )

    # 4. Key Data Flows
    document.add_heading('4. Key Data Flows', level=1)
    document.add_heading('4.1 Issue Drug Saga', level=2)
    document.add_paragraph('The happy path for issuing a drug involves the following steps:')
    
    steps = [
        'User submits request via Frontend.',
        'Orchestrator receives request and publishes "ReserveStockCommand".',
        'Inventory Service reserves stock and publishes "StockReservedEvent" (Success).',
        'Orchestrator receives success and publishes "CreateIssueCommand".',
        'Issue Service creates record and publishes "IssueCreatedEvent" (Success).',
        'Orchestrator commits the transaction and notifies Inventory to finalize.'
    ]
    
    for step in steps:
        document.add_paragraph(step, style='List Number')

    # 5. UI Design & Screenshots
    document.add_heading('5. UI Design & Screenshots', level=1)
    document.add_paragraph(
        'This section visualizes the key user interaction points. Please replace the placeholders with actual screenshots.'
    )

    document.add_heading('5.1 Home Dashboard', level=2)
    document.add_paragraph('Shows the landing page and overview of the application.')
    document.add_paragraph('[Insert Screenshot of HomePage.js / Dashboard]')

    document.add_heading('5.2 Inventory Management', level=2)
    document.add_paragraph('Displays the current stock levels, batches, and available quantities.')
    document.add_paragraph('[Insert Screenshot of InventoryPage.js]')

    document.add_heading('5.3 Patient Issue Form', level=2)
    document.add_paragraph('The primary screen for issuing drugs to patients, selecting batches, and submitting the request.')
    document.add_paragraph('[Insert Screenshot of IssuePage.js]')

    # Save
    output_path = 'd:/Projects Workspaces/terminator/md_files/High_Level_Design.docx'
    document.save(output_path)
    print(f"Document saved to {output_path}")

if __name__ == '__main__':
    create_hld()

# PPCL Printer Replacement Management

Internal web application for managing end-to-end printer replacement workflows — from request creation through pullback and closure.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Servlets, JSP, JSTL |
| Frontend | Bootstrap 4.5.2, jQuery, DataTables, Ace Admin v3.1.1 |
| Database | Oracle (JDBC) |

## Project Structure

```
src/main/java/com/ppcl/replacement/
├── constants/       # Stage codes, role names, SQL constants
├── dao/             # Data Access Objects
├── filter/          # AuthenticationFilter
├── model/           # DTOs / POJOs
├── scheduler/       # TAT percentage scheduler
├── servlet/         # servlets handling all HTTP endpoints
└── util/            # DB pool, JSON helpers, date utils

src/main/webapp/
├── css/             # Page-specific stylesheets
├── js/              # Page-specific JavaScript modules
├── login.jsp        # Entry point
├── views/
│   ├── am/          # Area Manager views (booking, letters, list)
│   ├── common/      # Shared header, footer, sidebar
│   ├── courier/     # Courier pincode mapping
│   ├── dashboard/   # KPI dashboard + event tracking
│   ├── replacement/ # Core workflow views
│   └── servicetl/   # Service TL list
└── WEB-INF/web.xml
```

## Workflow Stages

| #  | Stage                         | Role |
|----|-------------------------------|------|
| 1  | Request Raised                | CRO |
| 2  | Service TL Review             | SERVICE_TL |
| 3  | AM Commercial                 | AM_MANAGER |
| 4  | Quotation Sent                | AM |
| 6  | Printer Order Booking         | AM |
| 7  | Dispatch / Replacement Letter | AM |
| 8  | Installation                  | PRINTER_SERVICE |
| 9  | Pullback                      | LOGISTICS |
| 10 | QC Verification               | LOGISTICS |
| 11 | Credit Note                   | ACCOUNTS |
| 12 | Closure                       | AM_MANAGER |

## User Roles

`CRO` · `SERVICE_TL` · `AM` · `AM_MANAGER` · `LOGISTICS` · `ACCOUNTS` · `PRINTER_SERVICE`

## Key Features

- Multi-step replacement request creation (Client → Contact → Locations → Printers → Comments)
- Role-based views and actions at each workflow stage
- Printer order booking with automatic pullback call creation
- Pullback auto-routing: courier (if pincode mapping exists) or engineer (via client request)
- Replacement letter generation with digital signature support
- Courier-pincode mapping management
- TAT tracking and percentage scheduling
- KPI dashboard with event tracking
- Credit note workflow with approval chain
- Excel/PDF export support

## Build & Deploy

```bash
# Compile
mvn compile

# Package WAR
mvn clean package

# Output
target/replacement-management.war
```

Deploy the WAR to any Servlet 4.0 container (Tomcat 9+).

App URL: `http://localhost:8080/replacement-management/`

## DB Connection

Configured in `WEB-INF/web.xml`:

```
JDBC URL : jdbc:oracle:thin:@localhost:1521/FREEPDB1
User     : replacement_app
Password : {enter_password}
```

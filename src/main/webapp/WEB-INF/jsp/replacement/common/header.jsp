<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Replacement Management</title>

    <!-- include common vendor stylesheets & fontawesome -->
    <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">

    <!-- include fonts -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace-font.css">

    <!-- ace.css -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/ace-v3.1.1/dist/css/ace.css">

    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/select2-bootstrap-5-theme@1.3.0/dist/select2-bootstrap-5-theme.min.css" rel="stylesheet">
    <link href="https://cdn.datatables.net/1.10.24/css/dataTables.bootstrap4.min.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/replacement/style.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/replacement/app-common.css" rel="stylesheet">
    <style>
        /* Page Loading Overlay */
        .page-loader {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, rgba(255,255,255,0.95) 0%, rgba(240,242,245,0.95) 100%);
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            z-index: 9999;
            transition: opacity 0.4s ease, visibility 0.4s ease;
        }
        .page-loader.hidden {
            opacity: 0;
            visibility: hidden;
            pointer-events: none;
        }
        /* Stylish Dot Pulse Spinner */
        .page-loader__spinner {
            display: flex;
            gap: 8px;
        }
        .page-loader__spinner span {
            width: 12px;
            height: 12px;
            background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
            border-radius: 50%;
            animation: dotPulse 1.4s ease-in-out infinite;
            box-shadow: 0 2px 8px rgba(0,123,255,0.3);
        }
        .page-loader__spinner span:nth-child(1) { animation-delay: 0s; }
        .page-loader__spinner span:nth-child(2) { animation-delay: 0.2s; }
        .page-loader__spinner span:nth-child(3) { animation-delay: 0.4s; }
        @keyframes dotPulse {
            0%, 80%, 100% {
                transform: scale(0.6);
                opacity: 0.5;
            }
            40% {
                transform: scale(1.2);
                opacity: 1;
            }
        }
        .page-loader__text {
            margin-top: 20px;
            font-size: 15px;
            color: #495057;
            font-weight: 500;
            letter-spacing: 1px;
            text-transform: uppercase;
        }
        .page-loader__text::after {
            content: '';
            animation: loadingDots 1.5s infinite;
        }
        @keyframes loadingDots {
            0% { content: ''; }
            25% { content: '.'; }
            50% { content: '..'; }
            75% { content: '...'; }
            100% { content: ''; }
        }
    </style>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.1/dist/umd/popper.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.2/dist/js/bootstrap.min.js"></script>
    <script src="https://cdn.datatables.net/1.10.24/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/1.10.24/js/dataTables.bootstrap4.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/js/select2.min.js"></script>
</head>
<body class="bgc-white">
<!-- Page Loading Overlay -->
<div id="pageLoader" class="page-loader">
    <div class="page-loader__spinner">
        <span></span>
        <span></span>
        <span></span>
    </div>
    <div class="page-loader__text">Loading</div>
</div>

<div class="body-container">
    <!-- Navbar -->
    <nav class="navbar navbar-sm navbar-expand-lg navbar-fixed navbar-dark bgc-blue">
        <div class="navbar-inner brc-grey-l2 shadow-md">

            <!-- this button collapses/expands sidebar in desktop mode -->
            <button type="button" class="btn btn-burger align-self-center d-none d-xl-flex mx-2" data-toggle="sidebar" data-target="#sidebar" aria-controls="sidebar" aria-expanded="true" aria-label="Toggle sidebar">
                <span class="bars"></span>
            </button>

            <div class="d-flex h-100 align-items-center justify-content-xl-between">
                <!-- this button shows/hides sidebar in mobile mode -->
                <button type="button" class="btn btn-burger static burger-arrowed collapsed d-flex d-xl-none ml-2 bgc-h-white-l31" data-toggle-mobile="sidebar" data-target="#sidebar" aria-controls="sidebar" aria-expanded="false" aria-label="Toggle sidebar">
                    <span class="bars text-white"></span>
                </button>

                <a class="navbar-brand ml-2 text-white" href="<%= request.getContextPath() %>/views/replacement/dashboard">
                    PPCL PRM
                </a>
            </div>

            <!-- .navbar-menu toggler -->
            <button class="navbar-toggler mx-1 p-25" type="button" data-toggle="collapse" data-target="#navbarMenu" aria-controls="navbarMenu" aria-expanded="false" aria-label="Toggle navbar menu">
                <i class="fa fa-user text-white"></i>
            </button>

            <div class="ml-auto mr-lg-2 navbar-menu collapse navbar-collapse navbar-backdrop" id="navbarMenu">
                <div class="navbar-nav">
                    <ul class="nav nav-compact-2">
                        <li class="nav-item">
                            <a class="nav-link dropdown-toggle mr-1px" href="#">
                                <span class="text-white">${sessionScope.userName}</span>
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link dropdown-toggle pl-lg-3 pr-lg-4" href="<%= request.getContextPath() %>/logout">
                                <i class="fa fa-sign-out-alt text-white"></i>
                            </a>
                        </li>
                    </ul>
                </div>
            </div><!-- .navbar-menu -->

        </div><!-- .navbar-inner -->
    </nav>
    <div class="main-container bgc-white">

import React from 'react';
import { Link } from 'react-router-dom';

const HomePage = () => {
    return (
        <div className="home-container">
            <div className="hero-section">
                <h1>Hospital Management System</h1>
                <p>Select a service to proceed</p>
                <div className="action-buttons">
                    <Link to="/issue" className="btn-card">
                        <h3>Issue to Patient</h3>
                        <p>Issue drugs and items to patients</p>
                    </Link>
                    <Link to="/inventory" className="btn-card">
                        <h3>Drug Inventory</h3>
                        <p>Check current stock levels</p>
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default HomePage;

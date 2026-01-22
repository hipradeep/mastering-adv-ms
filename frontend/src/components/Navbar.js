import React from 'react';
import { Link } from 'react-router-dom';

const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="container">
                <Link to="/" className="logo">Hospital App</Link>
                <div className="nav-links">
                    <Link to="/issue" className="nav-item">Issue to Patient</Link>
                    <Link to="/inventory" className="nav-item">Drug Inventory</Link>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;

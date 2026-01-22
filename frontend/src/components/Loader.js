import React from 'react';
import '../styles/Loader.css';

const Loader = ({ message = "Processing..." }) => {
    return (
        <div className="loader-overlay">
            <div className="spinner"></div>
            <div className="loader-text">{message}</div>
        </div>
    );
};

export default Loader;

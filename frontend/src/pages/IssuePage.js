import React, { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { issueApi, inventoryApi, orchestratorApi } from '../services/api';
import Loader from '../components/Loader';

const IssuePage = () => {
    const [activeTab, setActiveTab] = useState('latestIssued');
    const [latestIssues, setLatestIssues] = useState([]);
    const [searchStoreId, setSearchStoreId] = useState('');
    const [availableStock, setAvailableStock] = useState([]);
    const [issueQty, setIssueQty] = useState({}); // Map of batchNo -> quantity

    // State to track active transaction for polling
    const [activeTransactionId, setActiveTransactionId] = useState(null);

    // cleanup interval on unmount
    React.useEffect(() => {
        let intervalId;

        if (activeTransactionId) {
            console.log(`Starting polling for Transaction ID: ${activeTransactionId}`);
            intervalId = setInterval(async () => {
                try {
                    const response = await orchestratorApi.get(`/orchestrator/status/${activeTransactionId}`);
                    const apiResponse = response.data;

                    if (!apiResponse.success) {
                        clearInterval(intervalId);
                        formik.setStatus({ error: `Error fetching status: ${apiResponse.message}` });
                        formik.setSubmitting(false);
                        setActiveTransactionId(null);
                        return;
                    }

                    const status = apiResponse.data;

                    if (status === 'COMPLETED') {
                        clearInterval(intervalId);
                        setAvailableStock([]);
                        setIssueQty({});
                        formik.resetForm(); // Clear the form
                        formik.setStatus({ success: `Issue Transaction Completed Successfully. Transaction ID: ${activeTransactionId}` }); // Set Status AFTER reset
                        formik.setSubmitting(false);
                        setActiveTransactionId(null);
                    } else if (status === 'STOCK_FAILED' || status === 'ISSUE_CREATION_FAILED') {
                        clearInterval(intervalId);
                        formik.setStatus({ error: `Issue Transaction Failed. Status: ${status}. Transaction ID: ${activeTransactionId}` });
                        formik.setSubmitting(false);
                        setActiveTransactionId(null);
                    } else if (status === 'STARTED' || status === 'STOCK_CONFIRMED') {
                        console.log(`Transaction ${activeTransactionId} is still processing...`);
                    } else {
                        clearInterval(intervalId);
                        formik.setStatus({ error: `Unknown status received: ${status}` });
                        formik.setSubmitting(false);
                        setActiveTransactionId(null);
                    }
                } catch (error) {
                    console.error("Polling error", error);
                    if (error.response && error.response.status === 404) {
                        clearInterval(intervalId);
                        formik.setStatus({ error: `Transaction Not Found during polling.` });
                        formik.setSubmitting(false);
                        setActiveTransactionId(null);
                    }
                }
            }, 2000);
        }

        return () => {
            if (intervalId) {
                console.log("Cleaning up polling interval");
                clearInterval(intervalId);
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activeTransactionId]);

    const formik = useFormik({
        initialValues: {
            storeId: '',
            puk: '',
            patientName: '',
            itemId: '',
            remarks: '',
            items: []
        },
        validationSchema: Yup.object({
            storeId: Yup.number().required('Store ID is required'),
            puk: Yup.number().required('PUK (Patient ID) is required'),
            patientName: Yup.string().required('Patient Name is required'),
        }),
        onSubmit: async (values, { setSubmitting, setStatus }) => {
            setStatus(null); // Clear previous messages

            // Artificial delay (500ms) so the Loader is visible even if the backend fails instantly
            await new Promise(resolve => setTimeout(resolve, 500));

            try {
                // Construct items list from issueQty map
                const itemsToIssue = Object.keys(issueQty).map(batchNo => {
                    return {
                        hstnumItembrandId: values.itemId,
                        hststrBatchSlNo: batchNo,
                        hstnumIssueQty: issueQty[batchNo],
                        hstnumStoreId: values.storeId,
                    };
                }).filter(item => item.hstnumIssueQty > 0);

                if (itemsToIssue.length === 0) {
                    setStatus({ error: 'Please enter issue quantity for at least one batch.' });
                    setSubmitting(false);
                    return;
                }

                const payload = {
                    hstnumStoreId: values.storeId,
                    hrgnumPuk: values.puk,
                    hststrPatientName: values.patientName,
                    gstrRemarks: values.remarks,
                    items: itemsToIssue
                };

                // Use Orchestrator API to trigger Saga
                const response = await orchestratorApi.post('/orchestrator/issue', payload);

                // Handle ApiResponse structure
                const apiResponse = response.data;

                if (apiResponse.success && apiResponse.data) {
                    const transactionId = apiResponse.data;
                    // Start Polling via useEffect
                    setActiveTransactionId(transactionId);
                    // IMPORTANT: We do NOT call setSubmitting(false) here. We leave it true so Loader stays up.
                } else {
                    setStatus({ error: 'Failed to initiate issue process: ' + (apiResponse.message || 'Unknown error') });
                    setSubmitting(false);
                }

            } catch (error) {
                setStatus({ error: 'Failed to create issue. ' + (error.response?.data?.message || error.message) });
                setSubmitting(false);
            }
            // Finally block removed because we want to wait for polling to finish
        },
    });

    const handleGetItems = async () => {
        if (!formik.values.storeId || !formik.values.itemId) {
            alert("Please enter Store ID and Item ID");
            return;
        }
        try {
            const response = await inventoryApi.get('/inventory/stock', {
                params: {
                    storeId: formik.values.storeId,
                    itemBrandId: formik.values.itemId
                }
            });
            if (response.data.success) {
                setAvailableStock(response.data.data);
                setIssueQty({}); // Reset quantities when new items fetched
            } else {
                setAvailableStock([]);
                alert(response.data.message || "No stock found");
            }
        } catch (error) {
            console.error("Failed to fetch stock", error);
            setAvailableStock([]);
            alert("Failed to fetch stock");
        }
    };

    const handleQtyChange = (batchNo, qty) => {
        setIssueQty(prev => ({
            ...prev,
            [batchNo]: qty
        }));
    };

    const fetchLatestIssues = React.useCallback(async () => {
        try {
            const response = await issueApi.get('/issue/latest', {
                params: {
                    storeId: searchStoreId
                }
            });
            if (response.data.status === 1 || response.data.success) {
                setLatestIssues(response.data.data);
            }
        } catch (error) {
            console.error("Failed to fetch latest issues", error);
            setLatestIssues([]);
        }
    }, [searchStoreId]); // Dependencies for useCallback

    React.useEffect(() => {
        if (activeTab === 'latestIssued') {
            fetchLatestIssues();
        }
    }, [activeTab, fetchLatestIssues]);

    return (
        <div className="page-container">
            <h2 className="page-title">Issue Drug to Patient</h2>

            <div className="tab-buttons" style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
                <button
                    className={`btn ${activeTab === 'latestIssued' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('latestIssued')}
                >
                    Latest Issued List
                </button>
                <button
                    className={`btn ${activeTab === 'issueToPatient' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('issueToPatient')}
                >
                    Issue to Patient
                </button>
            </div>

            {activeTab === 'latestIssued' && (
                <div className="table-container">
                    <h3>Latest Issued List</h3>
                    <form className="search-form" onSubmit={(e) => { e.preventDefault(); fetchLatestIssues(); }}>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Store ID</label>
                                <input
                                    type="number"
                                    value={searchStoreId}
                                    onChange={(e) => setSearchStoreId(e.target.value)}
                                />
                            </div>
                            <button type="submit" className="btn-secondary">Search</button>
                        </div>
                    </form>
                    {latestIssues.length > 0 ? (
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Store ID</th>
                                    <th>Issue No</th>
                                    <th>Item ID</th>
                                    <th>Batch No</th>
                                    <th>Quantity</th>
                                    <th>Issue Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                {latestIssues.map((item, index) => (
                                    <tr key={index}>
                                        <td>{item.hstnumStoreId}</td>
                                        <td>{item.hstnumIssueNo}</td>
                                        <td>{item.hstnumItembrandId}</td>
                                        <td>{item.hststrBatchSlNo}</td>
                                        <td>{item.hstnumIssueQty}</td>
                                        <td>{item.hstdtIssueDate ? new Date(item.hstdtIssueDate).toLocaleString() : '-'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    ) : (
                        <p className="no-data">No issued items found for this store.</p>
                    )}
                </div>
            )}

            {activeTab === 'issueToPatient' && (
                <>
                    {formik.status?.success && <div className="alert success">{formik.status.success}</div>}
                    {formik.status?.error && <div className="alert error">{formik.status.error}</div>}

                    <form onSubmit={formik.handleSubmit} className="form-card">

                        <div className="form-row">
                            <div className="form-group">
                                <label>Store Id</label>
                                <input
                                    type="number"
                                    name="storeId"
                                    value={formik.values.storeId}
                                    onChange={formik.handleChange}
                                    className={formik.touched.storeId && formik.errors.storeId ? 'input-error' : ''}
                                />
                                {formik.touched.storeId && formik.errors.storeId ? (
                                    <div className="error-msg">{formik.errors.storeId}</div>
                                ) : null}
                            </div>
                            <div className="form-group">
                                <label>PUK</label>
                                <input
                                    type="number"
                                    name="puk"
                                    value={formik.values.puk}
                                    onChange={formik.handleChange}
                                    className={formik.touched.puk && formik.errors.puk ? 'input-error' : ''}
                                />
                                {formik.touched.puk && formik.errors.puk ? (
                                    <div className="error-msg">{formik.errors.puk}</div>
                                ) : null}
                            </div>
                        </div>

                        <div className="form-group">
                            <label>Patient Name</label>
                            <input
                                type="text"
                                name="patientName"
                                value={formik.values.patientName}
                                onChange={formik.handleChange}
                                className={formik.touched.patientName && formik.errors.patientName ? 'input-error' : ''}
                            />
                            {formik.touched.patientName && formik.errors.patientName ? (
                                <div className="error-msg">{formik.errors.patientName}</div>
                            ) : null}
                        </div>

                        <div className="form-row" style={{ alignItems: 'flex-end' }}>
                            <div className="form-group" style={{ flex: 2 }}>
                                <label>ItemID</label>
                                <input
                                    type="number"
                                    name="itemId"
                                    value={formik.values.itemId}
                                    onChange={formik.handleChange}
                                />
                            </div>
                            <div className="form-group" style={{ flex: 1 }}>
                                <button type="button" className="btn-secondary" onClick={handleGetItems}>
                                    get items
                                </button>
                            </div>
                        </div>

                        {availableStock.length > 0 && (
                            <div className="stock-section" style={{ border: '1px solid #ccc', padding: '10px', marginTop: '15px' }}>
                                <h4>Available item on store with batch</h4>
                                <table className="data-table" style={{ width: '100%' }}>
                                    <thead>
                                        <tr>
                                            <th>BatchNo</th>
                                            <th>inhandqty</th>
                                            <th>enter issue qty</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {availableStock.map((stock) => (
                                            <tr key={stock.hststrBatchNo}>
                                                <td>{stock.hststrBatchNo}</td>
                                                <td>{stock.hstnumInhandQty}</td>
                                                <td>
                                                    <input
                                                        type="number"
                                                        value={issueQty[stock.hststrBatchNo] || ''}
                                                        onChange={(e) => handleQtyChange(stock.hststrBatchNo, e.target.value)}
                                                        style={{ width: '100px' }}
                                                        min="0"
                                                        max={stock.hstnumInhandQty}
                                                    />
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        <div className="form-group" style={{ marginTop: '15px' }}>
                            <label>Remark</label>
                            <textarea
                                name="remarks"
                                value={formik.values.remarks}
                                onChange={formik.handleChange}
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn-primary"
                            disabled={formik.isSubmitting || activeTransactionId}
                            style={{ width: '100%', opacity: (formik.isSubmitting || activeTransactionId) ? 0.7 : 1, cursor: (formik.isSubmitting || activeTransactionId) ? 'not-allowed' : 'pointer' }}
                            onClick={(e) => {
                                if (formik.isSubmitting || activeTransactionId) {
                                    e.preventDefault();
                                    return;
                                }
                            }}
                        >
                            {(formik.isSubmitting || activeTransactionId) ? 'Processing Transaction...' : 'Issue Drug'}
                        </button>
                    </form>
                    {(formik.isSubmitting || activeTransactionId) && <Loader message="Processing Transaction... Please wait." />}
                </>
            )}
        </div>
    );
};

export default IssuePage;

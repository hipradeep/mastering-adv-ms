import React, { useState } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { inventoryApi } from '../services/api';

const InventoryPage = () => {
    const [stockList, setStockList] = useState([]);

    const formik = useFormik({
        initialValues: {
            storeId: '',
            itemBrandId: ''
        },
        validationSchema: Yup.object({
            storeId: Yup.number().nullable(),
            itemBrandId: Yup.number().nullable()
        }),
        onSubmit: async (values, { setSubmitting, setStatus }) => {
            try {
                const response = await inventoryApi.get('/inventory/stock', {
                    params: {
                        storeId: values.storeId,
                        itemBrandId: values.itemBrandId
                    }
                });
                if (response.data.success) {
                    setStockList(response.data.data); // Assuming ApiResponse wrapper
                    setStatus({ success: 'Data fetched successfully' });
                } else {
                    setStockList([]);
                    setStatus({ error: response.data.message });
                }
            } catch (error) {
                setStockList([]);
                setStatus({ error: 'Failed to fetch inventory. ' + (error.response?.data?.message || error.message) });
            } finally {
                setSubmitting(false);
            }
        },
    });

    const [activeTab, setActiveTab] = useState('checkStock');

    const updateFormik = useFormik({
        initialValues: {
            storeId: '',
            itemBrandId: '',
            batchNo: '',
            quantity: ''
        },
        validationSchema: Yup.object({
            storeId: Yup.number().required('Store ID is required'),
            itemBrandId: Yup.number().required('Item ID is required'),
            batchNo: Yup.string().required('Batch No is required'),
            quantity: Yup.number().required('Quantity is required').min(0, 'Quantity must be non-negative')
        }),
        onSubmit: async (values, { setSubmitting, setStatus }) => {
            try {
                // Using URLSearchParams for x-www-form-urlencoded data as implied by @RequestParam
                const params = new URLSearchParams();
                params.append('storeId', values.storeId);
                params.append('itemBrandId', values.itemBrandId);
                params.append('batchNo', values.batchNo);
                params.append('quantity', values.quantity);

                const response = await inventoryApi.post('/inventory/stock/update', params, {
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    }
                });

                if (response.data.success) {
                    setStatus({ success: 'Stock updated successfully!' });
                } else {
                    setStatus({ error: response.data.message || 'Update failed' });
                }
            } catch (error) {
                setStatus({ error: 'Failed to update stock. ' + (error.response?.data?.message || error.message) });
            } finally {
                setSubmitting(false);
            }
        },
    });

    return (
        <div className="page-container">
            <h2 className="page-title">Drug Inventory Check</h2>
            <div className="tab-buttons" style={{ marginBottom: '20px', display: 'flex', gap: '10px' }}>
                <button
                    className={`btn ${activeTab === 'checkStock' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('checkStock')}
                >
                    Check Stock
                </button>
                <button
                    className={`btn ${activeTab === 'updateInventory' ? 'btn-primary' : 'btn-secondary'}`}
                    onClick={() => setActiveTab('updateInventory')}
                >
                    Update Inventory
                </button>
            </div>

            {activeTab === 'checkStock' && (
                <>
                    <form onSubmit={formik.handleSubmit} className="search-form">
                        <div className="form-row">
                            <div className="form-group">
                                {/* Hospital Code is hidden and defaults to 998 */}
                                <label>Store ID</label>
                                <input
                                    type="number"
                                    name="storeId"
                                    value={formik.values.storeId}
                                    onChange={formik.handleChange}
                                />
                                {formik.touched.storeId && formik.errors.storeId ? (
                                    <div className="error-msg">{formik.errors.storeId}</div>
                                ) : null}
                            </div>
                            <div className="form-group">
                                <label>Item ID</label>
                                <input
                                    type="number"
                                    name="itemBrandId"
                                    value={formik.values.itemBrandId}
                                    onChange={formik.handleChange}
                                />
                                {formik.touched.itemBrandId && formik.errors.itemBrandId ? (
                                    <div className="error-msg">{formik.errors.itemBrandId}</div>
                                ) : null}
                            </div>
                            <button type="submit" className="btn-secondary" disabled={formik.isSubmitting}>
                                Check Stock
                            </button>
                        </div>
                    </form>

                    {formik.status?.error && <div className="alert error">{formik.status.error}</div>}

                    <div className="table-container">
                        {stockList.length > 0 ? (
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Store ID</th>
                                        <th>Item ID</th>
                                        <th>Batch No</th>
                                        <th>In-Hand Qty</th>
                                        <th>Rate</th>
                                        <th>Expiry Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stockList.map((stock, index) => (
                                        <tr key={index}>
                                            <td>{stock.hstnumStoreId}</td>
                                            <td>{stock.hstnumItembrandId}</td>
                                            <td>{stock.hststrBatchNo}</td>
                                            <td>{stock.hstnumInhandQty}</td>
                                            <td>{stock.hstnumRate}</td>
                                            <td>{new Date(stock.hstdtExpiryDate).toLocaleDateString()}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            !formik.isSubmitting && formik.submitCount > 0 && !formik.status?.error && <p className="no-data">No stock data found.</p>
                        )}
                    </div>
                </>
            )}

            {activeTab === 'updateInventory' && (
                <div>
                    <h2 className="page-title">Update Inventory</h2>
                    <form onSubmit={updateFormik.handleSubmit} className="search-form" style={{ flexDirection: 'column', alignItems: 'flex-start' }}>
                        <div className="form-row">
                            <div className="form-group">
                                <label>Store ID</label>
                                <input
                                    type="number"
                                    name="storeId"
                                    value={updateFormik.values.storeId}
                                    onChange={updateFormik.handleChange}
                                />
                                {updateFormik.touched.storeId && updateFormik.errors.storeId ? (
                                    <div className="error-msg">{updateFormik.errors.storeId}</div>
                                ) : null}
                            </div>
                            <div className="form-group">
                                <label>Item ID</label>
                                <input
                                    type="number"
                                    name="itemBrandId"
                                    value={updateFormik.values.itemBrandId}
                                    onChange={updateFormik.handleChange}
                                />
                                {updateFormik.touched.itemBrandId && updateFormik.errors.itemBrandId ? (
                                    <div className="error-msg">{updateFormik.errors.itemBrandId}</div>
                                ) : null}
                            </div>
                            <div className="form-group">
                                <label>Batch No</label>
                                <input
                                    type="text"
                                    name="batchNo"
                                    value={updateFormik.values.batchNo}
                                    onChange={updateFormik.handleChange}
                                />
                                {updateFormik.touched.batchNo && updateFormik.errors.batchNo ? (
                                    <div className="error-msg">{updateFormik.errors.batchNo}</div>
                                ) : null}
                            </div>
                            <div className="form-group">
                                <label>New Quantity</label>
                                <input
                                    type="number"
                                    name="quantity"
                                    value={updateFormik.values.quantity}
                                    onChange={updateFormik.handleChange}
                                />
                                {updateFormik.touched.quantity && updateFormik.errors.quantity ? (
                                    <div className="error-msg">{updateFormik.errors.quantity}</div>
                                ) : null}
                            </div>
                        </div>
                        <button type="submit" className="btn-primary" disabled={updateFormik.isSubmitting} style={{ marginTop: '15px' }}>
                            Update Stock
                        </button>
                    </form>
                    {updateFormik.status?.success && <div className="alert success">{updateFormik.status.success}</div>}
                    {updateFormik.status?.error && <div className="alert error">{updateFormik.status.error}</div>}
                </div>
            )}
        </div>
    );
};

export default InventoryPage;

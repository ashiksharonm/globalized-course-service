import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import CoursePreviewPage from './pages/CoursePreviewPage';
import './i18n'; // Must be imported to initialize i18next
import './index.css';

/**
 * Root application component.
 *
 * <Suspense> wraps all routes because i18next lazy-loads locale bundles.
 * Without Suspense, components would attempt to render before the locale file
 * finishes loading, producing untranslated key strings in the UI.
 */
const App: React.FC = () => {
    return (
        <BrowserRouter>
            <Suspense fallback={<div className="loading-splash">Loading...</div>}>
                <Routes>
                    <Route path="/" element={<CoursePreviewPage />} />
                    <Route path="/courses/:courseId" element={<CoursePreviewPage />} />
                </Routes>
            </Suspense>
        </BrowserRouter>
    );
};

export default App;

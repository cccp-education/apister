import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Slider from './slider';
import SliderDetail from './slider-detail';
import SliderUpdate from './slider-update';
import SliderDeleteDialog from './slider-delete-dialog';

const SliderRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Slider />} />
    <Route path="new" element={<SliderUpdate />} />
    <Route path=":id">
      <Route index element={<SliderDetail />} />
      <Route path="edit" element={<SliderUpdate />} />
      <Route path="delete" element={<SliderDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SliderRoutes;

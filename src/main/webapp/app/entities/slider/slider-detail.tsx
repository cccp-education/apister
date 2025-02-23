import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './slider.reducer';

export const SliderDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const sliderEntity = useAppSelector(state => state.slider.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="sliderDetailsHeading">
          <Translate contentKey="apisterApp.slider.detail.title">Slider</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{sliderEntity.id}</dd>
          <dt>
            <span id="presentation">
              <Translate contentKey="apisterApp.slider.presentation">Presentation</Translate>
            </span>
          </dt>
          <dd>{sliderEntity.presentation}</dd>
          <dt>
            <Translate contentKey="apisterApp.slider.user">User</Translate>
          </dt>
          <dd>{sliderEntity.user ? sliderEntity.user.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/slider" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/slider/${sliderEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SliderDetail;

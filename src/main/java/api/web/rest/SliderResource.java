package api.web.rest;

import api.domain.criteria.SliderCriteria;
import api.repository.SliderRepository;
import api.service.SliderService;
import api.service.dto.SliderDTO;
import api.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link api.domain.Slider}.
 */
@RestController
@RequestMapping("/api/sliders")
public class SliderResource {

    private static final Logger LOG = LoggerFactory.getLogger(SliderResource.class);

    private static final String ENTITY_NAME = "slider";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SliderService sliderService;

    private final SliderRepository sliderRepository;

    public SliderResource(SliderService sliderService, SliderRepository sliderRepository) {
        this.sliderService = sliderService;
        this.sliderRepository = sliderRepository;
    }

    /**
     * {@code POST  /sliders} : Create a new slider.
     *
     * @param sliderDTO the sliderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new sliderDTO, or with status {@code 400 (Bad Request)} if the slider has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public Mono<ResponseEntity<SliderDTO>> createSlider(@Valid @RequestBody SliderDTO sliderDTO) throws URISyntaxException {
        LOG.debug("REST request to save Slider : {}", sliderDTO);
        if (sliderDTO.getId() != null) {
            throw new BadRequestAlertException("A new slider cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return sliderService
            .save(sliderDTO)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/sliders/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /sliders/:id} : Updates an existing slider.
     *
     * @param id the id of the sliderDTO to save.
     * @param sliderDTO the sliderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sliderDTO,
     * or with status {@code 400 (Bad Request)} if the sliderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the sliderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<SliderDTO>> updateSlider(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody SliderDTO sliderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Slider : {}, {}", id, sliderDTO);
        if (sliderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sliderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sliderRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return sliderService
                    .update(sliderDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result)
                    );
            });
    }

    /**
     * {@code PATCH  /sliders/:id} : Partial updates given fields of an existing slider, field will ignore if it is null
     *
     * @param id the id of the sliderDTO to save.
     * @param sliderDTO the sliderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated sliderDTO,
     * or with status {@code 400 (Bad Request)} if the sliderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the sliderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the sliderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<SliderDTO>> partialUpdateSlider(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody SliderDTO sliderDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Slider partially : {}, {}", id, sliderDTO);
        if (sliderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, sliderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return sliderRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                Mono<SliderDTO> result = sliderService.partialUpdate(sliderDTO);

                return result
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(res ->
                        ResponseEntity.ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                            .body(res)
                    );
            });
    }

    /**
     * {@code GET  /sliders} : get all the sliders.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of sliders in body.
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<List<SliderDTO>>> getAllSliders(
        SliderCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        LOG.debug("REST request to get Sliders by criteria: {}", criteria);
        return sliderService
            .countByCriteria(criteria)
            .zipWith(sliderService.findByCriteria(criteria, pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity.ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
    }

    /**
     * {@code GET  /sliders/count} : count all the sliders.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public Mono<ResponseEntity<Long>> countSliders(SliderCriteria criteria) {
        LOG.debug("REST request to count Sliders by criteria: {}", criteria);
        return sliderService.countByCriteria(criteria).map(count -> ResponseEntity.status(HttpStatus.OK).body(count));
    }

    /**
     * {@code GET  /sliders/:id} : get the "id" slider.
     *
     * @param id the id of the sliderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the sliderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SliderDTO>> getSlider(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Slider : {}", id);
        Mono<SliderDTO> sliderDTO = sliderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(sliderDTO);
    }

    /**
     * {@code DELETE  /sliders/:id} : delete the "id" slider.
     *
     * @param id the id of the sliderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteSlider(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Slider : {}", id);
        return sliderService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
                )
            );
    }
}

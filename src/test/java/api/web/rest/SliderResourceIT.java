package api.web.rest;

import static api.domain.SliderAsserts.*;
import static api.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import api.IntegrationTest;
import api.domain.Slider;
import api.domain.User;
import api.repository.EntityManager;
import api.repository.SliderRepository;
import api.repository.UserRepository;
import api.repository.UserRepository;
import api.service.SliderService;
import api.service.dto.SliderDTO;
import api.service.mapper.SliderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

/**
 * Integration tests for the {@link SliderResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class SliderResourceIT {

    private static final String DEFAULT_PRESENTATION = "AAAAAAAAAA";
    private static final String UPDATED_PRESENTATION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/sliders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private SliderRepository sliderRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private SliderRepository sliderRepositoryMock;

    @Autowired
    private SliderMapper sliderMapper;

    @Mock
    private SliderService sliderServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Slider slider;

    private Slider insertedSlider;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Slider createEntity(EntityManager em) {
        Slider slider = new Slider().presentation(DEFAULT_PRESENTATION);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        slider.setUser(user);
        return slider;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Slider createUpdatedEntity(EntityManager em) {
        Slider updatedSlider = new Slider().presentation(UPDATED_PRESENTATION);
        // Add required entity
        User user = em.insert(UserResourceIT.createEntity()).block();
        updatedSlider.setUser(user);
        return updatedSlider;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Slider.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        UserResourceIT.deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        slider = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedSlider != null) {
            sliderRepository.delete(insertedSlider).block();
            insertedSlider = null;
        }
        deleteEntities(em);
        userRepository.deleteAllUserAuthorities().block();
        userRepository.deleteAll().block();
    }

    @Test
    void createSlider() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);
        var returnedSliderDTO = webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(SliderDTO.class)
            .returnResult()
            .getResponseBody();

        // Validate the Slider in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedSlider = sliderMapper.toEntity(returnedSliderDTO);
        assertSliderUpdatableFieldsEquals(returnedSlider, getPersistedSlider(returnedSlider));

        insertedSlider = returnedSlider;
    }

    @Test
    void createSliderWithExistingId() throws Exception {
        // Create the Slider with an existing ID
        slider.setId(1L);
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    void checkPresentationIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        slider.setPresentation(null);

        // Create the Slider, which fails.
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    void getAllSliders() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(slider.getId().intValue()))
            .jsonPath("$.[*].presentation")
            .value(hasItem(DEFAULT_PRESENTATION));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSlidersWithEagerRelationshipsIsEnabled() {
        when(sliderServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(sliderServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllSlidersWithEagerRelationshipsIsNotEnabled() {
        when(sliderServiceMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=false").exchange().expectStatus().isOk();
        verify(sliderRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getSlider() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get the slider
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, slider.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(slider.getId().intValue()))
            .jsonPath("$.presentation")
            .value(is(DEFAULT_PRESENTATION));
    }

    @Test
    void getSlidersByIdFiltering() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        Long id = slider.getId();

        defaultSliderFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultSliderFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultSliderFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    void getAllSlidersByPresentationIsEqualToSomething() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList where presentation equals to
        defaultSliderFiltering("presentation.equals=" + DEFAULT_PRESENTATION, "presentation.equals=" + UPDATED_PRESENTATION);
    }

    @Test
    void getAllSlidersByPresentationIsInShouldWork() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList where presentation in
        defaultSliderFiltering(
            "presentation.in=" + DEFAULT_PRESENTATION + "," + UPDATED_PRESENTATION,
            "presentation.in=" + UPDATED_PRESENTATION
        );
    }

    @Test
    void getAllSlidersByPresentationIsNullOrNotNull() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList where presentation is not null
        defaultSliderFiltering("presentation.specified=true", "presentation.specified=false");
    }

    @Test
    void getAllSlidersByPresentationContainsSomething() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList where presentation contains
        defaultSliderFiltering("presentation.contains=" + DEFAULT_PRESENTATION, "presentation.contains=" + UPDATED_PRESENTATION);
    }

    @Test
    void getAllSlidersByPresentationNotContainsSomething() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        // Get all the sliderList where presentation does not contain
        defaultSliderFiltering(
            "presentation.doesNotContain=" + UPDATED_PRESENTATION,
            "presentation.doesNotContain=" + DEFAULT_PRESENTATION
        );
    }

    @Test
    void getAllSlidersByUserIsEqualToSomething() {
        User user = UserResourceIT.createEntity();
        userRepository.save(user).block();
        Long userId = user.getId();
        slider.setUserId(userId);
        insertedSlider = sliderRepository.save(slider).block();
        // Get all the sliderList where user equals to userId
        defaultSliderShouldBeFound("userId.equals=" + userId);

        // Get all the sliderList where user equals to (userId + 1)
        defaultSliderShouldNotBeFound("userId.equals=" + (userId + 1));
    }

    private void defaultSliderFiltering(String shouldBeFound, String shouldNotBeFound) {
        defaultSliderShouldBeFound(shouldBeFound);
        defaultSliderShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSliderShouldBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(slider.getId().intValue()))
            .jsonPath("$.[*].presentation")
            .value(hasItem(DEFAULT_PRESENTATION));

        // Check, that the count call also returns 1
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(1));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSliderShouldNotBeFound(String filter) {
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .isArray()
            .jsonPath("$")
            .isEmpty();

        // Check, that the count call also returns 0
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "/count?sort=id,desc&" + filter)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$")
            .value(is(0));
    }

    @Test
    void getNonExistingSlider() {
        // Get the slider
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_PROBLEM_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingSlider() throws Exception {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the slider
        Slider updatedSlider = sliderRepository.findById(slider.getId()).block();
        updatedSlider.presentation(UPDATED_PRESENTATION);
        SliderDTO sliderDTO = sliderMapper.toDto(updatedSlider);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, sliderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedSliderToMatchAllProperties(updatedSlider);
    }

    @Test
    void putNonExistingSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, sliderDTO.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateSliderWithPatch() throws Exception {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the slider using partial update
        Slider partialUpdatedSlider = new Slider();
        partialUpdatedSlider.setId(slider.getId());

        partialUpdatedSlider.presentation(UPDATED_PRESENTATION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSlider.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSlider))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Slider in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSliderUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedSlider, slider), getPersistedSlider(slider));
    }

    @Test
    void fullUpdateSliderWithPatch() throws Exception {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the slider using partial update
        Slider partialUpdatedSlider = new Slider();
        partialUpdatedSlider.setId(slider.getId());

        partialUpdatedSlider.presentation(UPDATED_PRESENTATION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedSlider.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(partialUpdatedSlider))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Slider in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertSliderUpdatableFieldsEquals(partialUpdatedSlider, getPersistedSlider(partialUpdatedSlider));
    }

    @Test
    void patchNonExistingSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, sliderDTO.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, longCount.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamSlider() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        slider.setId(longCount.incrementAndGet());

        // Create the Slider
        SliderDTO sliderDTO = sliderMapper.toDto(slider);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(om.writeValueAsBytes(sliderDTO))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Slider in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteSlider() {
        // Initialize the database
        insertedSlider = sliderRepository.save(slider).block();

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the slider
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, slider.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return sliderRepository.count().block();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Slider getPersistedSlider(Slider slider) {
        return sliderRepository.findById(slider.getId()).block();
    }

    protected void assertPersistedSliderToMatchAllProperties(Slider expectedSlider) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSliderAllPropertiesEquals(expectedSlider, getPersistedSlider(expectedSlider));
        assertSliderUpdatableFieldsEquals(expectedSlider, getPersistedSlider(expectedSlider));
    }

    protected void assertPersistedSliderToMatchUpdatableProperties(Slider expectedSlider) {
        // Test fails because reactive api returns an empty object instead of null
        // assertSliderAllUpdatablePropertiesEquals(expectedSlider, getPersistedSlider(expectedSlider));
        assertSliderUpdatableFieldsEquals(expectedSlider, getPersistedSlider(expectedSlider));
    }
}

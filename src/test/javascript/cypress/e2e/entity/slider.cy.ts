import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Slider e2e test', () => {
  const sliderPageUrl = '/slider';
  const sliderPageUrlPattern = new RegExp('/slider(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  // const sliderSample = {"presentation":"compenser jusqu’à ce que propre"};

  let slider;
  // let user;

  beforeEach(() => {
    cy.login(username, password);
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/users',
      body: {"login":"W","firstName":"Adelin","lastName":"Leclerc","email":"Balthazar.Barre92@hotmail.fr","imageUrl":"athlète pin-pon","langKey":"subito"},
    }).then(({ body }) => {
      user = body;
    });
  });
   */

  beforeEach(() => {
    cy.intercept('GET', '/api/sliders+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/sliders').as('postEntityRequest');
    cy.intercept('DELETE', '/api/sliders/*').as('deleteEntityRequest');
  });

  /* Disabled due to incompatibility
  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/users', {
      statusCode: 200,
      body: [user],
    });

  });
   */

  afterEach(() => {
    if (slider) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/sliders/${slider.id}`,
      }).then(() => {
        slider = undefined;
      });
    }
  });

  /* Disabled due to incompatibility
  afterEach(() => {
    if (user) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/users/${user.id}`,
      }).then(() => {
        user = undefined;
      });
    }
  });
   */

  it('Sliders menu should load Sliders page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('slider');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Slider').should('exist');
    cy.url().should('match', sliderPageUrlPattern);
  });

  describe('Slider page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(sliderPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Slider page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/slider/new$'));
        cy.getEntityCreateUpdateHeading('Slider');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sliderPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      /* Disabled due to incompatibility
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/sliders',
          body: {
            ...sliderSample,
            user: user,
          },
        }).then(({ body }) => {
          slider = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/sliders+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/sliders?page=0&size=20>; rel="last",<http://localhost/api/sliders?page=0&size=20>; rel="first"',
              },
              body: [slider],
            }
          ).as('entitiesRequestInternal');
        });

        cy.visit(sliderPageUrl);

        cy.wait('@entitiesRequestInternal');
      });
       */

      beforeEach(function () {
        cy.visit(sliderPageUrl);

        cy.wait('@entitiesRequest').then(({ response }) => {
          if (response?.body.length === 0) {
            this.skip();
          }
        });
      });

      it('detail button click should load details Slider page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('slider');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sliderPageUrlPattern);
      });

      it('edit button click should load edit Slider page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Slider');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sliderPageUrlPattern);
      });

      it('edit button click should load edit Slider page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Slider');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sliderPageUrlPattern);
      });

      // Reason: cannot create a required entity with relationship with required relationships.
      it.skip('last delete button click should delete instance of Slider', () => {
        cy.intercept('GET', '/api/sliders/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('slider').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', sliderPageUrlPattern);

        slider = undefined;
      });
    });
  });

  describe('new Slider page', () => {
    beforeEach(() => {
      cy.visit(`${sliderPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Slider');
    });

    // Reason: cannot create a required entity with relationship with required relationships.
    it.skip('should create an instance of Slider', () => {
      cy.get(`[data-cy="presentation"]`).type('trop membre de l’équipe');
      cy.get(`[data-cy="presentation"]`).should('have.value', 'trop membre de l’équipe');

      cy.get(`[data-cy="user"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        slider = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', sliderPageUrlPattern);
    });
  });
});

package com.jouiwnl.core.repository;

import com.jouiwnl.core.pageable.Page;
import com.jouiwnl.core.pageable.Pageable;
import com.jouiwnl.core.querydsl.Filterable;
import com.jouiwnl.core.querydsl.NaturalQueryParser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.jouiwnl.core.dto.DatabaseEntity;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 *
 */
@Component
public class BasicRepository {

    private static final String FK_MESSAGE_DEFAULT = "Não é possível excluir este item, o mesmo está em uso em outro registro";

    private static final String POSTGRES_23503_RESTRICAO_INTEGRIDADE = "23503";
    private static final String ORACLE_23000_RESTRICAO_INTEGRIDADE = "23000";

    private EntityManager em;

    public BasicRepository(EntityManager em) {
        this.em = em;
    }

    public <T> List<T> findAll(final Class<T> entityClass, final Predicate... where) {
        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT);
        return query.from(new PathBuilderFactory().create(entityClass)).where(where).fetch();
    }

    public <T extends Filterable> List<T> findAll(final Class<T> entityClass, String filter) {
        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT);
        final BooleanBuilder where = NaturalQueryParser.parse(filter, entityClass);

        return query.from(new PathBuilderFactory().create(entityClass)).where(where).fetch();
    }

    public <T extends Filterable> Page<T> findAll(
            final Class<T> entityClass,
            final Pageable pageable,
            final String filter) {

        final BooleanBuilder where = NaturalQueryParser.parse(filter, entityClass);

        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT)
                .from(new PathBuilderFactory().create(entityClass))
                .where(where);

        final int limit = pageable.getLimit();

        int offset = pageable.getOffset();

        final long total = query.fetchCount();

        if (offset > total) {
            offset = ((int) (total / limit)) * limit;
        } else if (offset == total) {
            offset = Math.max(offset - limit, 0);
        }

        query.offset(offset);
        query.limit(limit);

        return new Page<>(query.fetch(), pageable, total);
    }

    public <T> Page<T> findAll(
            final Class<T> entityClass,
            final Pageable pageable,
            final Predicate... where) {

        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT)
                .from(new PathBuilderFactory().create(entityClass))
                .where(where);

        final int limit = pageable.getLimit();

        int offset = pageable.getOffset();

        final long total = query.fetchCount();

        if (offset > total) {
            offset = ((int) (total / limit)) * limit;
        } else if (offset == total) {
            offset = Math.max(offset - limit, 0);
        }

        query.offset(offset);
        query.limit(limit);

        return new Page<>(query.fetch(), pageable, total);
    }

    public <T> T findOne(Class<T> entityClass, Predicate... where) {
        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT);
        return query.from(new PathBuilderFactory().create(entityClass)).where(where).limit(1).fetchOne();
    }

    public <T extends DatabaseEntity<?>> T save(T entity) {
        Objects.requireNonNull(entity, "entity can't be null");
        boolean isNew = Objects.isNull(entity.getId());

        if (!isNew) {
            em.merge(entity);
        } else {
            em.persist(entity);
        }

        em.flush();
        return entity;
    }

    public <T extends DatabaseEntity<?>> void remove(T entity) {
        try {
            em.remove(entity);
            em.flush();
        } catch (PersistenceException e) {
            handlerRemoveException(e);
        }
    }

    public <T> boolean exists(Class<T> entityClass, final Predicate... where) {
        JPAQuery<T> query = new JPAQuery<T>(em, JPQLTemplates.DEFAULT);
        return query.from(new PathBuilderFactory().create(entityClass)).where(where).limit(1L).fetchCount() > 0;
    }

    protected void handlerRemoveException(PersistenceException e) {
        if (!(e.getCause() instanceof ConstraintViolationException)) {
            throw e;
        }

        SQLException violationException = (SQLException) e.getCause().getCause();
        if (POSTGRES_23503_RESTRICAO_INTEGRIDADE.equals(violationException.getSQLState())) {
            throw new RuntimeException(FK_MESSAGE_DEFAULT);
        }
        if (ORACLE_23000_RESTRICAO_INTEGRIDADE.equals(violationException.getSQLState())) {
            throw new RuntimeException(FK_MESSAGE_DEFAULT);
        }
    }

}

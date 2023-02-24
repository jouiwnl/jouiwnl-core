package com.jouiwnl.core.pageable;

/**
 * Representa as configurações de uma paginação.
 */
public final class Pageable {

    public static final int DEFAULT_LIMIT = 25;
    public static final int MAX_LIMIT = 1000;

    private final int offset;
    private final int limit;

    private Pageable(int offset, int limit) {
        if (0 > offset) {
            throw new IllegalArgumentException("Offset must not be less than zero!");
        }
        if (0 > limit) {
            throw new IllegalArgumentException("Limit must not be less than zero!");
        } else if (limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Limit must not be greater than " + MAX_LIMIT + "!");
        } else if (0 == limit) {
            limit = DEFAULT_LIMIT;
        }

        this.offset = offset;
        this.limit = limit;
    }

    public static Pageable of(int offset, int limit) {
        return new Pageable(offset, limit);
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pageable)) {
            return false;
        }
        final Pageable that = (Pageable) obj;
        final boolean offsetEqual = this.offset == that.offset;
        final boolean limitEqual = this.limit == that.limit;
        return offsetEqual && limitEqual;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.offset;
        hash = 53 * hash + this.limit;
        return hash;
    }

}

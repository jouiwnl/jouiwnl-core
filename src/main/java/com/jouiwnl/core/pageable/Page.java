package com.jouiwnl.core.pageable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * Representa uma página de uma paginação de resultados, assim como a
 * configuração da mesma.
 *
 * @param <T> Objeto paginado.
 */
public class Page<T> implements Iterable<T> {

    private final Collection<T> content;
    private final Pageable pageable;
    private final long total;

    public Page(final Collection<T> content, final Pageable pageable, long total) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(pageable);

        this.content = content;
        this.pageable = pageable;
        this.total = total;
    }

    public static <T> Page<T> empty() {
        return new Page<>(Collections.emptyList(), Pageable.of(0, 0), 0);
    }

    public Pageable getPageable() {
        return pageable;
    }

    public Collection<T> getContent() {
        return Collections.unmodifiableCollection(content);
    }

    public long getTotal() {
        return total;
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public String toString() {
        String contentType = "UNKNOWN";
        if (!content.isEmpty()) {
            contentType = content.iterator().next().getClass().getName();
        }

        return String.format("Offset %d containing %d %s instances",
                pageable.getOffset(),
                pageable.getLimit(),
                contentType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Page)) {
            return false;
        }

        Page<?> page = (Page<?>) o;

        return new EqualsBuilder()
                .append(total, page.total)
                .append(content, page.content)
                .append(pageable, page.pageable)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(content)
                .append(pageable)
                .append(total)
                .toHashCode();
    }
}


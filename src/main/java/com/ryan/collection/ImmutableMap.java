package com.ryan.collection;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Immutable {@link Map}. This implementation provides a predictable iteration
 * order by remembering the order in which keys were inserted into the
 * {@code Map} (insertion-order). Note that insertion order is not affected if
 * a key is re-inserted into the {@code Map}. Accepts {@code null} values and
 * the {@code null} key.
 *
 * @param <K> the type of the {@code Map}'s keys.
 * @param <V> the type of the {@code Map}'s values.
 *
 * @author Osman KOCAK
 */
public final class ImmutableMap<K, V> extends AbstractMap<K, V> implements Serializable
{
	private static final long serialVersionUID = 458645004066047051L;

	/**
	 * {@code ImmutableMap} builder. Not thread-safe.
	 *
	 * @param <K> the type of the {@code Map}'s keys.
	 * @param <V> the type of the {@code Map}'s values.
	 */
	public static final class Builder<K, V>
	{
		private final Map<K, V> inner = new LinkedHashMap<K, V>();

		/**
		 * Associates the specified value with the specified key in the
		 * {@code Map} being built.
		 *
		 * @param key the key.
		 * @param value the value.
		 *
		 * @return this object.
		 */
		public Builder<K, V> put(K key, V value)
		{
			inner.put(key, value);
			return this;
		}

		/**
		 * Copies all of the mappings from the specified {@code Map} to
		 * the {@code Map} being built.
		 *
		 * @param map mappings to be copied.
		 *
		 * @return this object.
		 *
		 * @throws NullPointerException if {@code map} is {@code null}.
		 */
		public Builder<K, V> put(Map<? extends K, ? extends V> map)
		{
			inner.putAll(map);
			return this;
		}

		/**
		 * Finalizes the creation of the {@code ImmutableMap}.
		 *
		 * @return an instance of {@code ImmutableMap}.
		 */
		public Map<K, V> build()
		{
			return new ImmutableMap<K, V>(inner);
		}
	}

	/**
	 * Creates a new {@code ImmutableSet} from the given {@code Iterable}.
	 *
	 * @param <K> the type of the {@code Map}'s keys.
	 * @param <V> the type of the {@code Map}'s values.
	 * @param map the source {@code Map}.
	 *
	 * @return the created {@code ImmutableMap}.
	 *
	 * @throws NullPointerException if {@code map} is {@code null}.
	 */
	public static <K, V> Map<K, V> copyOf(Map<? extends K, ? extends V> map)
	{
		return new ImmutableMap<K, V>(new LinkedHashMap<K, V>(map));
	}

	private final Map<K, V> inner;

	private ImmutableMap(Map<K, V> inner)
	{
		this.inner = inner;
	}

	@Override
	public int size()
	{
		return inner.size();
	}

	@Override
	public boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return inner.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return inner.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return inner.get(key);
	}

	@Override
	public V put(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet()
	{
		return ImmutableSet.copyOf(inner.keySet());
	}

	@Override
	public Collection<V> values()
	{
		return ImmutableList.copyOf(inner.values());
	}

	@Override
	public Set<Entry<K, V>> entrySet()
	{
		return ImmutableSet.copyOf(inner.entrySet());
	}
}

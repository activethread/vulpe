/**
 * Vulpe Framework - Copyright (c) Active Thread
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vulpe.commons.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.vulpe.exception.VulpeSystemException;

/**
 * Utility class to control objects in cache.
 * 
 * @author <a href="mailto:fabio.viana@vulpe.org">Fábio Viana</a>
 * @author <a href="mailto:felipe@vulpe.org">Geraldo Felipe</a>
 */
@SuppressWarnings( { "unchecked" })
public final class VulpeCacheHelper {
	private static final VulpeCacheHelper INSTANCE = new VulpeCacheHelper();

	public static VulpeCacheHelper getInstance() {
		return INSTANCE;
	}

	private VulpeCacheHelper() {
	}

	private transient final Map<Object, Object> cache = Collections
			.synchronizedMap(new HashMap<Object, Object>());

	public void put(final Object key, final Object instance) {
		cache.put(key, instance);
	}

	public <T> T get(final Object key) {
		return (T) cache.get(key);
	}

	public <T> T getInstance(final Class<T> classe) {
		if (!contains(classe)) {
			try {
				put(classe, classe.newInstance());
			} catch (Exception e) {
				throw new VulpeSystemException(e);
			}
		}
		return (T) get(classe);
	}

	public boolean contains(final Object key) {
		return cache.containsKey(key);
	}

	public void remove(final Object key) {
		cache.remove(key);
	}
}
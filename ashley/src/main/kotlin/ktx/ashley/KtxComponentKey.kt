package ktx.ashley

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentType
import com.badlogic.ashley.core.Entity
import kotlin.reflect.KClass

open class KtxComponentKey<T : Component>
private constructor(val type: ComponentType) {
    constructor(type: Class<T>) : this(ComponentType.getFor(type))
    constructor(type: KClass<T>) : this(type.java)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <T : Component> Entity.get(key: KtxComponentKey<T>): T = getComponent(key.type)
package extension

import org.mockito.Mockito

inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)

inline fun <reified T : Any> spy(): T = Mockito.spy(T::class.java)
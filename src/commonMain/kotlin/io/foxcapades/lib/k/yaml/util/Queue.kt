package io.foxcapades.lib.k.yaml.util

/**
 * Generic Queue
 */
class Queue<T> {
  private var raw: Array<Any?>

  private var head: Int

  /**
   * Number of items currently held in this [Queue].
   */
  var size: Int
    private set

  /**
   * Current capacity of this [Queue].
   *
   * Capacity differs from size in that the capacity is the max number of
   * elements that may be added to the `Queue` before a resize and reallocation
   * of the underlying data structure is required.
   */
  val capacity: Int
    get() = raw.size

  /**
   * Resize Scale Factor
   *
   * Controls how quickly the underlying data structure grows when a resize is
   * necessary.
   *
   * If this value is set to `2.0`, for example, the queue's capacity will
   * double every time a resize is required.
   */
  val scaleFactor: Float

  /**
   * Max Allowed Capacity
   *
   * The maximum size the underlying data structure is permitted to grow to.
   *
   * If [size] == [maxCapacity] and an attempt is made to push another value
   * into the queue, an exception will be thrown.
   */
  val maxCapacity: Int

  /**
   * The amount of space in the queue that may be filled before the queue needs
   * to resize to accommodate more values.
   */
  val spaceBeforeResize: Int
    get() = raw.size - size

  /**
   * The amount of space in the queue that may be filled before the configured
   * [maxCapacity] is reached.
   */
  val spaceBeforeLimit: Int
    get() = maxCapacity - size

  /**
   * Whether this queue currently holds zero items.
   */
  inline val isEmpty: Boolean
    get() = size == 0

  /**
   * Whether this queue currently holds one or more items.
   */
  inline val isNotEmpty: Boolean
    get() = size > 0

  /**
   * The index of the last item in the queue.
   */
  inline val lastIndex: Int
    get() = size - 1

  /**
   * Constructs a new [Queue] instance.
   *
   * @param initialCapacity The initial size for the raw data buffer underlying
   * the new [Queue] instance.
   *
   * This value must be less than or equal to the value of [maxCapacity].
   *
   * @param scaleFactor The resize scale factor.  Controls how quickly the raw
   * data buffer underlying the new [Queue] instance will grow when the current
   * capacity is exceeded.
   *
   * This value must be greater than `1.0`.
   *
   * @param maxCapacity The maximum size the new [Queue] instance is allowed to
   * grow to.
   */
  @Suppress("UNCHECKED_CAST")
  constructor(
    initialCapacity: Int   = 16,
    scaleFactor:     Float = 1.5f,
    maxCapacity:     Int   = Int.MAX_VALUE
  ) {
    if (initialCapacity > maxCapacity)
      throw IllegalArgumentException("attempted to construct a Queue instance with an initial capacity value ($initialCapacity) that is greater than the given max capacity value ($maxCapacity)")
    if (scaleFactor <= 1)
      throw IllegalArgumentException("attempted to construct a Queue instance with a scale factor value that is less than or equal to 1")

    this.raw = arrayOfNulls(initialCapacity)
    this.head = 0
    this.size = 0
    this.scaleFactor = scaleFactor
    this.maxCapacity = maxCapacity
  }

  /**
   * Peeks the first element in the [Queue].
   *
   * To "peek" the value is to return it without removing it from the `Queue`.
   *
   * **Example**
   * ```
   * // Given a queue of
   * val queue = Queue('A', 'B', 'C')
   *
   * // #peek will return the first element
   * require(queue.peek() == 'A')
   *
   * // without removing it.
   * require(queue.size == 3)
   * ```
   *
   * @return The value currently at the head of the queue.
   *
   * @throws NoSuchElementException If the `Queue` is empty when this method is
   * called.
   */
  @Suppress("UNCHECKED_CAST")
  fun peek(): T =
    if (isEmpty)
      throw NoSuchElementException("attempted to peek the head of an empty Queue instance")
    else
      raw[head] as T

  /**
   * Pops the first element from the [Queue] and returns it.
   *
   * **Example**
   * ```
   * // Given a queue of
   * val queue = Queue('A', 'B', 'C')
   *
   * // #pop will return the first element
   * require(queue.pop() == 'A')
   *
   * // and will remove it
   * require(queue.size == 2)
   * ```
   *
   * @return The value removed from the head of this `Queue`.
   *
   * @throws NoSuchElementException If the `Queue` is empty when this method is
   * called.
   */
  @Suppress("UNCHECKED_CAST")
  fun pop(): T {
    if (isEmpty)
      throw NoSuchElementException("attempted to pop the head of an empty Queue instance")

    val out = raw[head] as T
    raw[head] = null
    head = idx(1)
    size--
    return out
  }

  /**
   * Returns the element at the given index from in this [Queue].
   *
   * The target element will not be removed from the queue.
   *
   * **Example**
   * ```
   * // Given a queue of
   * val queue = Queue('A', 'B', 'C')
   *
   * // #get will return the target element
   * require(queue[2] == 'C')
   *
   * // without removing it.
   * require(queue.size == 3)
   * ```
   *
   * @param i Index of the `Queue` element to return.
   *
   * @return The element in this `Queue` that is at the given index.
   *
   * @throws IndexOutOfBoundsException If [i] is less than zero or greater than
   * or equal to [size].
   */
  @Suppress("UNCHECKED_CAST")
  operator fun get(i: Int): T =
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to access an item at index $i of a Queue instance of size $size")
    else
      raw[idx(i)] as T

  /**
   * Replaces the element at the given index in this [Queue].
   *
   * @param i Index of the `Queue` element to replace.
   *
   * @param value Value to replace the previous `Queue` element with.
   *
   * @throws IndexOutOfBoundsException If [i] is less than zero or greater than
   * or equal to [size].
   */
  operator fun set(i: Int, value: T) {
    if (i < 0 || i >= size)
      throw IndexOutOfBoundsException("attempted to set an item at index $i of a Queue instance of size $size")
    else
      raw[idx(i)] = value
  }

  /**
   * Pushes a new value onto the tail end of this [Queue] instance.
   *
   * @param value Value to append to the `Queue`.
   *
   * @throws IllegalStateException If appending the new item would cause the
   * [Queue] to grow to a [size] that is greater than [maxCapacity].
   */
  fun push(value: T) {
    try {
      ensureCapacity(size + 1)
    } catch (e: IllegalArgumentException) {
      throw IllegalStateException(e.message)
    }

    raw[idx(size++)] = value
  }

  /**
   * Alias for [push]
   *
   * @see push
   */
  @Suppress("NOTHING_TO_INLINE")
  inline operator fun plusAssign(value: T) = push(value)

  /**
   * Removes all elements from this [Queue] instance.
   *
   * This method does not change the capacity of this `Queue`.
   */
  fun clear() {
    var i = 0
    while (i < size)
      raw[idx(i++)] = null

    head = 0
    size = 0
  }

  /**
   * Ensures that this [Queue] has a capacity of at least the given
   * [minCapacity] value.
   *
   * If the `Queue` already has a capacity that is greater than or equal to the
   * given minimum required capacity value, then this method does nothing.
   *
   * If the current capacity of the `Queue` is less than the given minimum
   * required capacity value, then this method will reallocate the underlying
   * data buffer to a new capacity that is at least [minCapacity].
   *
   * The actual new capacity of the buffer is decided, in part, by the
   * configured [scaleFactor].  This means that if the current capacity value
   * multiplied by the configured `scaleFactor` is greater than `minCapacity`,
   * the calculated value will be used instead of `minCapacity`.
   *
   * @param minCapacity Minimum required capacity for this `Queue` instance.
   *
   * @throws IllegalArgumentException If [minCapacity] is greater than the
   * configured [maxCapacity] value.
   */
  fun ensureCapacity(minCapacity: Int) {
    if (minCapacity > maxCapacity)
      throw IllegalArgumentException("attempted to resize a Queue instance to a new capacity ($minCapacity) that is greater than the configured max capacity ($maxCapacity)")

    if (raw.size >= minCapacity)
      return

    raw = toArray(min(max(minCapacity, (raw.size.toFloat() * scaleFactor).toInt()), maxCapacity))
  }

  @Suppress("UNCHECKED_CAST")
  fun toArray(fn: (Int) -> Array<T?>): Array<T> {
    val out = fn(size)

    if (isNotEmpty) {
      val tail = idx(lastIndex)

      if (head <= tail) {
        (raw as Array<T?>).copyInto(out, 0, head, tail + 1)
      } else {
        (raw as Array<T?>).copyInto(out, 0, head, raw.size)
        (raw as Array<T?>).copyInto(out, raw.size - head, 0, tail + 1)
      }
    }

    return out as Array<T>
  }

  fun popToArray(fn: (Int) -> Array<T?>): Array<T> {
    val out = toArray(fn)
    clear()
    return out
  }

  @Suppress("UNCHECKED_CAST")
  private fun toArray(size: Int): Array<Any?> {
    val out = arrayOfNulls<Any>(size)

    if (isNotEmpty) {
      val tail = idx(lastIndex)

      if (head <= tail) {
        raw.copyInto(out, 0, head, tail + 1)
      } else {
        raw.copyInto(out, 0, head, raw.size)
        raw.copyInto(out, raw.size - head, 0, tail + 1)
      }
    }

    return out
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun idx(i: Int) = if (head + i >= raw.size) head + i - raw.size else head + i
}
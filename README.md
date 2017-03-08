# Lock Free Concurrent Circular Buffer

It supports multiple independent readers, so many consumers can get updates to this circular buffer.

It is lock free, so neither writers nor readers will block.  If you try to `take()` on a buffer with no updates a null is returned, and if you `drain()` a buffer with no updates you get an empty list.

I recommend you give it a spin if you want a (simple) circular buffer.  It does detect buffer wraparounds, but will in this case reset the reader to the end of the buffer, losing any intermediate updates. Tune your buffer size accordingly.

Take a look at the unit tests to get an idea on how to use this buffer.

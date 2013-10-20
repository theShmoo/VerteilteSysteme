package message;

import java.io.Serializable;

/**
 * Classes implementing {@code Request} interface are intended to have <i>request</i> semantics.<br/>
 * A request is an object that is sent to a remote machine in order to trigger actions or retrieve data.
 *
 * Once a {@code Request} is created, it should not be modifiable anymore. Thus it is recommended but not required that
 * {@code Request}s are immutable and prevent intentional as well as unintentional modifications of their state.<br/>
 * Furthermore, they implement the {@link Serializable} allowing implementations to be serialized.
 *
 * @see Response
 */
public interface Request extends Serializable {
}

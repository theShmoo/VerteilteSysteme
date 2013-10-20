package message;

import java.io.Serializable;

/**
 * Classes implementing {@code Response} interface are intended to have <i>response</i> semantics.<br/>
 * A {@code Response} is an object that is sent to a remote machine in order to send some payload requested before.
 *
 * Once a {@code Response} is created, it should not be modifiable anymore. Thus it is recommended but not required that
 * {@code Response}s are immutable and prevent intentional as well as unintentional modifications of their state.<br/>
 * Furthermore, they implement the {@link Serializable} allowing implementations to be serialized.
 *
 * @see Request
 */
public interface Response extends Serializable {
}

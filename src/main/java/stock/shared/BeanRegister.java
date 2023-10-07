package stock.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import stock.repository.MainShareRepository;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanRegister {

    public static final MainShareRepository MAIN_SHARE_REPOSITORY = new MainShareRepository();

}

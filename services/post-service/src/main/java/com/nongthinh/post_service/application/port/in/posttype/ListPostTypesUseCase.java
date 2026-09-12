package com.nongthinh.post_service.application.port.in.posttype;

import com.nongthinh.post_service.application.view.PostTypeView;
import java.util.List;

public interface ListPostTypesUseCase {
    List<PostTypeView> execute(boolean activeOnly);
}

package com.astra.user.application.port.in;

import java.util.List;

public interface ListUsersQuery {

    List<AdminUserResponse> list();
}

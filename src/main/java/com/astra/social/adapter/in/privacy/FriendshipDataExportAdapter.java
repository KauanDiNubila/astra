package com.astra.social.adapter.in.privacy;

import com.astra.social.application.port.in.ExportFriendshipsQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
class FriendshipDataExportAdapter implements ExportUserDataPort {

    private final ExportFriendshipsQuery exportFriendshipsQuery;

    FriendshipDataExportAdapter(ExportFriendshipsQuery exportFriendshipsQuery) {
        this.exportFriendshipsQuery = exportFriendshipsQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("friendships", exportFriendshipsQuery.export(userId));
    }
}

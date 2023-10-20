package darling.domain;

import lombok.Builder;

@Builder
public record MainShare(String uid, String figi, String ticker, String name, Integer lot, boolean isTrade) {
}
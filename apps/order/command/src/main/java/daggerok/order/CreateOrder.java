package daggerok.order;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@ToString
@RequiredArgsConstructor
public class CreateOrder {
  final UUID id;
  final List<String> orderItemIds;
}

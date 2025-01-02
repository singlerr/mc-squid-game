package io.github.singlerr.admin;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModelStorage {

  private Map<UUID, EntityReference> entities = new HashMap<>();

}

# mockapproval
Showcase how to approve calls to a Mockito mock/spy

## Open issue
Maps are printed out as { key1 = value1, key2 = value2 }
Since hashCode of Strings (and maybe other objects differs each time) there is no gurantee of the order, so the next time the map is printed out it could be { key2 = value2, key1 = value1 }
We could also scrub this (sorting by key) but then we don't know if the map has been a map with or without ordering, so if the map was as LinkedHashMap { key1 = value1, key2 = value2 } and { key2 = value2, key1 = value1 } are different map contents while on a HasMap they wouldn't
Solution? I don't have an idea yet but overriding the toString for some type like HashMap in any way or by getting stable, reproducable hashCodes for objects like Strings


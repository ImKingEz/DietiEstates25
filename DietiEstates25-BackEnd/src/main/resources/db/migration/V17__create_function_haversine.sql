CREATE OR REPLACE FUNCTION haversine(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
) RETURNS DOUBLE PRECISION AS $$
DECLARE
radius INTEGER := 6371; -- Earth radius in kilometers
    dist DOUBLE PRECISION := 0.0;
    dLat DOUBLE PRECISION := radians(lat2 - lat1);
    dLon DOUBLE PRECISION := radians(lon2 - lon1);
    a DOUBLE PRECISION;
    c DOUBLE PRECISION;
BEGIN
    a := sin(dLat / 2) * sin(dLat / 2) +
         cos(radians(lat1)) * cos(radians(lat2)) *
         sin(dLon / 2) * sin(dLon / 2);
    c := 2 * asin(sqrt(a));
    dist := radius * c;
RETURN dist * 1000; -- distanza in metri
END;
$$ LANGUAGE plpgsql;
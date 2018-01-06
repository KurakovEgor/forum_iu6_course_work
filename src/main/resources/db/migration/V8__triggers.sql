CREATE OR REPLACE FUNCTION add_children() RETURNS trigger AS $add_children$
BEGIN
	NEW.children := array_append(NEW.children, NEW.id::INTEGER);
	RETURN NEW;
END;
$add_children$ LANGUAGE plpgsql;

CREATE TRIGGER add_children BEFORE INSERT ON posts
	FOR EACH ROW EXECUTE PROCEDURE add_children();

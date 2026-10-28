USE master;
GO
GRANT VIEW ANY DEFINITION TO proyecto_grupo4;
GO

USE DB_ProyectoGrupo4;
GO
GRANT VIEW DEFINITION ON DATABASE::DB_ProyectoGrupo4 TO proyecto_grupo4;
GRANT SELECT ON SCHEMA::dbo TO proyecto_grupo4;
-- Necesario para UPDATE STATISTICS, ALTER INDEX REBUILD y sp_refreshsqlmodule
GRANT ALTER ON SCHEMA::dbo TO proyecto_grupo4;
GO
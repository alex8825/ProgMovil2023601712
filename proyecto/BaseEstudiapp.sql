usuarios | CREATE TABLE `usuarios` (
  `id` varchar(36) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasena` varchar(255) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 


grupos | CREATE TABLE `grupos` (
  `id_grupo` varchar(36) NOT NULL,
  `codigo_grupo` varchar(10) NOT NULL,
  `nombre_grupo` varchar(100) DEFAULT 'Grupo de Estudio',
  `fecha_creacion` datetime DEFAULT current_timestamp(),
  `id_admin_usuario` varchar(36) NOT NULL,
  PRIMARY KEY (`id_grupo`),
  UNIQUE KEY `codigo_grupo` (`codigo_grupo`),
  KEY `fk_grupo_admin_usuario` (`id_admin_usuario`),
  CONSTRAINT `fk_grupo_admin_usuario` FOREIGN KEY (`id_admin_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 

usuarios_grupos | CREATE TABLE `usuarios_grupos` (
  `id_usuario` varchar(36) NOT NULL,
  `id_grupo` varchar(36) NOT NULL,
  `fecha_union` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id_usuario`,`id_grupo`),
  KEY `usuarios_grupos_ibfk_2` (`id_grupo`),
  CONSTRAINT `usuarios_grupos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `usuarios_grupos_ibfk_2` FOREIGN KEY (`id_grupo`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 

tareas | CREATE TABLE `tareas` (
  `id` varchar(36) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `materia_id` varchar(36) NOT NULL,
  `usuario_id` varchar(36) NOT NULL,
  `grupo_id` varchar(36) DEFAULT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL DEFAULT '00:00:00',
  `completada` tinyint(1) DEFAULT 0,
  `prioridad` varchar(10) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_tarea_materia` (`materia_id`),
  KEY `fk_tarea_usuario` (`usuario_id`),
  KEY `fk_tarea_grupo` (`grupo_id`),
  CONSTRAINT `fk_tarea_grupo` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE,
  CONSTRAINT `fk_tarea_materia` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tarea_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci |

proyectos | CREATE TABLE `proyectos` (
  `id` varchar(36) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `materia_id` varchar(36) NOT NULL,
  `usuario_id` varchar(36) NOT NULL,
  `grupo_id` varchar(36) DEFAULT NULL,
  `fechaLimite` date NOT NULL,
  `horaLimite` time NOT NULL DEFAULT '00:00:00',
  `estado` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_proyecto_materia` (`materia_id`),
  KEY `fk_proyecto_usuario` (`usuario_id`),
  KEY `fk_proyecto_grupo` (`grupo_id`),
  CONSTRAINT `fk_proyecto_grupo` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE,
  CONSTRAINT `fk_proyecto_materia` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_proyecto_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 

materias | CREATE TABLE `materias` (
  `id` varchar(36) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `usuario_id` varchar(36) DEFAULT NULL,
  `grupo_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_materia_usuario` (`usuario_id`),
  KEY `fk_materia_grupo` (`grupo_id`),
  CONSTRAINT `fk_materia_grupo` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE,
  CONSTRAINT `fk_materia_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 

examenes | CREATE TABLE `examenes` (
  `id` varchar(36) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `materia_id` varchar(36) NOT NULL,
  `usuario_id` varchar(36) NOT NULL,
  `grupo_id` varchar(36) DEFAULT NULL,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `estado` varchar(20) DEFAULT 'Próximo',
  PRIMARY KEY (`id`),
  KEY `fk_examen_materia` (`materia_id`),
  KEY `fk_examen_usuario` (`usuario_id`),
  KEY `fk_examen_grupo` (`grupo_id`),
  CONSTRAINT `fk_examen_grupo` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE,
  CONSTRAINT `fk_examen_materia` FOREIGN KEY (`materia_id`) REFERENCES `materias` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_examen_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 

eventos | CREATE TABLE `eventos` (
  `id` varchar(36) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `fecha` date NOT NULL,
  `completado` tinyint(1) DEFAULT 0,
  `usuario_id` varchar(36) NOT NULL,
  `grupo_id` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_evento_usuario` (`usuario_id`),
  KEY `fk_evento_grupo` (`grupo_id`),
  CONSTRAINT `fk_evento_grupo` FOREIGN KEY (`grupo_id`) REFERENCES `grupos` (`id_grupo`) ON DELETE CASCADE,
  CONSTRAINT `fk_evento_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
